package com.orbitapay.carteira.persistence.trava;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.orbitapay.carteira.application.exception.TravaIndisponivelException;
import com.orbitapay.carteira.persistence.CarteiraDocument;

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

    public CarteiraDocument travar(String clienteId, String dono) {
        Instant limite = Instant.now().plus(ESPERA_MAXIMA);
        LOG.info("ANTES DO LOCK | carteira={} dono={}", clienteId, dono);
        while (true) {
            Instant agora = Instant.now();
            Query carteiraLivre = new Query(Criteria.where("clienteId").is(clienteId).orOperator(
                    Criteria.where("trava").isNull(),
                    Criteria.where("trava.expiraEm").lt(agora)));
            Update travar = new Update().set("trava", new TravaDocument(dono, agora, agora.plus(VALIDADE_DA_TRAVA)));

            CarteiraDocument carteira = mongo.findAndModify(carteiraLivre, travar, CarteiraDocument.class);
            if (carteira != null) {
                LOG.info("LOCK ADQUIRIDO | carteira={} dono={}", clienteId, dono);
                return carteira;
            }
            if (Instant.now().isAfter(limite)) {
                LOG.warn("LOCK EXPIROU A ESPERA | carteira={} dono={}", clienteId, dono);
                throw new TravaIndisponivelException("carteira:" + clienteId);
            }
            esperar();
        }
    }

    public void salvarELiberar(CarteiraDocument carteira, String dono) {
        Query minhaTrava = new Query(Criteria.where("clienteId").is(carteira.clienteId()).and("trava.dono").is(dono));
        if (mongo.findAndReplace(minhaTrava, carteira) == null) {
            LOG.warn("LOCK PERDIDO | carteira={} dono={}", carteira.clienteId(), dono);
            throw new TravaIndisponivelException("carteira:" + carteira.clienteId());
        }
        LOG.info("SAVE + LOCK LIBERADO | carteira={} dono={}", carteira.clienteId(), dono);
    }

    public void liberar(String clienteId, String dono) {
        Query minhaTrava = new Query(Criteria.where("clienteId").is(clienteId).and("trava.dono").is(dono));
        mongo.updateFirst(minhaTrava, new Update().unset("trava"), CarteiraDocument.class);
        LOG.info("LOCK LIBERADO SEM ALTERACAO | carteira={} dono={}", clienteId, dono);
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
