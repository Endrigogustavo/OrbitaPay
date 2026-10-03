package com.orbitapay.negociacao.persistence.trava;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.orbitapay.negociacao.application.exception.TravaIndisponivelException;
import com.orbitapay.negociacao.domain.exception.RecursoNaoEncontradoException;
import com.orbitapay.negociacao.persistence.AtivoNegociavelDocument;

@Component
public class TravaPessimistaMongo {

    private static final Logger LOG = LoggerFactory.getLogger(TravaPessimistaMongo.class);
    private static final Duration ESPERA_MAXIMA = Duration.ofSeconds(10);
    private static final Duration VALIDADE_DA_TRAVA = Duration.ofSeconds(30);
    private static final long INTERVALO_ENTRE_TENTATIVAS_MS = 20;

    private final MongoTemplate mongo;

    public TravaPessimistaMongo(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    public AtivoNegociavelDocument travar(String ticker, String dono) {
        Instant limite = Instant.now().plus(ESPERA_MAXIMA);
        LOG.info("ANTES DO LOCK | ativo={} dono={}", ticker, dono);
        while (true) {
            Instant agora = Instant.now();
            Query ativoLivre = new Query(Criteria.where("_id").is(ticker).orOperator(
                    Criteria.where("trava").isNull(),
                    Criteria.where("trava.expiraEm").lt(agora)));
            Update travar = new Update().set("trava", new TravaDocument(dono, agora, agora.plus(VALIDADE_DA_TRAVA)));

            AtivoNegociavelDocument ativo = mongo.findAndModify(ativoLivre, travar, AtivoNegociavelDocument.class);
            if (ativo != null) {
                LOG.info("LOCK ADQUIRIDO | ativo={} dono={}", ticker, dono);
                return ativo;
            }
            if (!mongo.exists(new Query(Criteria.where("_id").is(ticker)), AtivoNegociavelDocument.class)) {
                throw new RecursoNaoEncontradoException("Ativo não negociado: " + ticker);
            }
            if (Instant.now().isAfter(limite)) {
                LOG.warn("LOCK EXPIROU A ESPERA | ativo={} dono={}", ticker, dono);
                throw new TravaIndisponivelException("ativo:" + ticker);
            }
            esperar();
        }
    }

    public void salvarELiberar(AtivoNegociavelDocument ativo, String dono) {
        Query minhaTrava = new Query(Criteria.where("_id").is(ativo.ticker()).and("trava.dono").is(dono));
        if (mongo.findAndReplace(minhaTrava, ativo) == null) {
            LOG.warn("LOCK PERDIDO | ativo={} dono={}", ativo.ticker(), dono);
            throw new TravaIndisponivelException("ativo:" + ativo.ticker());
        }
        LOG.info("SAVE + LOCK LIBERADO | ativo={} dono={}", ativo.ticker(), dono);
    }

    public void liberar(String ticker, String dono) {
        Query minhaTrava = new Query(Criteria.where("_id").is(ticker).and("trava.dono").is(dono));
        mongo.updateFirst(minhaTrava, new Update().unset("trava"), AtivoNegociavelDocument.class);
        LOG.info("LOCK LIBERADO SEM ALTERACAO | ativo={} dono={}", ticker, dono);
    }

    private static void esperar() {
        try {
            Thread.sleep(INTERVALO_ENTRE_TENTATIVAS_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Espera pela trava interrompida", e);
        }
    }
}
