package com.orbitapay.carteira.persistence.trava;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.orbitapay.carteira.application.exception.TravaIndisponivelException;

@Component
public class TravaPessimistaMongo {

    private static final Logger LOG = LoggerFactory.getLogger(TravaPessimistaMongo.class);
    private static final String CAMPO = "trava";

    private final MongoTemplate mongo;
    private final Duration esperaMaxima;
    private final Duration expiracao;
    private final Clock relogio;

    public TravaPessimistaMongo(MongoTemplate mongo,
            @Value("${orbita.trava.espera-maxima-ms}") long esperaMaximaMs,
            @Value("${orbita.trava.expiracao-ms}") long expiracaoMs,
            Clock relogio) {
        this.mongo = mongo;
        this.esperaMaxima = Duration.ofMillis(esperaMaximaMs);
        this.expiracao = Duration.ofMillis(expiracaoMs);
        this.relogio = relogio;
    }

    public <T> T adquirir(Class<T> tipo, Criteria filtro, String recurso, String dono) {
        long inicio = System.nanoTime();
        long limite = inicio + esperaMaxima.toNanos();
        int tentativa = 0;
        LOG.info("ANTES DO LOCK | recurso={} dono={}", recurso, dono);
        while (true) {
            Instant agora = Instant.now(relogio);
            Query livre = new Query(new Criteria().andOperator(filtro, new Criteria().orOperator(
                    Criteria.where(CAMPO).isNull(), Criteria.where(CAMPO + ".expiraEm").lt(agora))));
            Update travar = new Update().set(CAMPO, new TravaDocument(dono, agora, agora.plus(expiracao)));
            T documento = mongo.findAndModify(livre, travar, tipo);
            if (documento != null) {
                LOG.info("LOCK ADQUIRIDO | recurso={} dono={} esperou={}ms tentativas={}", recurso, dono,
                        (System.nanoTime() - inicio) / 1_000_000, tentativa + 1);
                return documento;
            }
            if (!mongo.exists(new Query(filtro), tipo)) {
                return null;
            }
            if (System.nanoTime() > limite) {
                LOG.warn("LOCK EXPIROU A ESPERA | recurso={} dono={}", recurso, dono);
                throw new TravaIndisponivelException(recurso);
            }
            aguardar(tentativa++);
        }
    }

    public <T> boolean substituirELiberar(Class<T> tipo, Criteria filtro, String recurso, String dono, T documento) {
        Query minhaTrava = new Query(new Criteria().andOperator(filtro, Criteria.where(CAMPO + ".dono").is(dono)));
        boolean substituiu = mongo.findAndReplace(minhaTrava, documento) != null;
        LOG.info("{} | recurso={} dono={}", substituiu ? "SAVE + LOCK LIBERADO" : "LOCK PERDIDO", recurso, dono);
        return substituiu;
    }

    public <T> void liberar(Class<T> tipo, Criteria filtro, String recurso, String dono) {
        Query minhaTrava = new Query(new Criteria().andOperator(filtro, Criteria.where(CAMPO + ".dono").is(dono)));
        mongo.updateFirst(minhaTrava, new Update().unset(CAMPO), tipo);
        LOG.info("LOCK LIBERADO SEM ALTERACAO | recurso={} dono={}", recurso, dono);
    }

    public static String novoDono() {
        return UUID.randomUUID().toString();
    }

    private static void aguardar(int tentativa) {
        long base = Math.min(5L << Math.min(tentativa, 5), 120L);
        try {
            Thread.sleep(base + ThreadLocalRandom.current().nextLong(10));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Espera pela trava interrompida", e);
        }
    }
}
