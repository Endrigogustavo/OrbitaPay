package com.orbitapay.contas.persistence.trava;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.orbitapay.contas.application.exception.TravaIndisponivelException;
import com.orbitapay.contas.domain.exception.ContaNaoEncontradaException;
import com.orbitapay.contas.persistence.ContaDocument;

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

    public ContaDocument travar(String clienteId, String dono) {
        Instant limite = Instant.now().plus(ESPERA_MAXIMA);
        LOG.info("ANTES DO LOCK | conta={} dono={}", clienteId, dono);
        while (true) {
            Instant agora = Instant.now();
            Query contaLivre = new Query(Criteria.where("clienteId").is(clienteId).orOperator(
                    Criteria.where("trava").isNull(),
                    Criteria.where("trava.expiraEm").lt(agora)));
            Update travar = new Update().set("trava", new TravaDocument(dono, agora, agora.plus(VALIDADE_DA_TRAVA)));

            ContaDocument conta = mongo.findAndModify(contaLivre, travar, ContaDocument.class);
            if (conta != null) {
                LOG.info("LOCK ADQUIRIDO | conta={} dono={}", clienteId, dono);
                return conta;
            }
            if (!mongo.exists(new Query(Criteria.where("clienteId").is(clienteId)), ContaDocument.class)) {
                throw new ContaNaoEncontradaException(clienteId);
            }
            if (Instant.now().isAfter(limite)) {
                LOG.warn("LOCK EXPIROU A ESPERA | conta={} dono={}", clienteId, dono);
                throw new TravaIndisponivelException("conta:" + clienteId);
            }
            esperar();
        }
    }

    public void salvarELiberar(ContaDocument conta, String dono) {
        Query minhaTrava = new Query(Criteria.where("clienteId").is(conta.clienteId()).and("trava.dono").is(dono));
        if (mongo.findAndReplace(minhaTrava, conta) == null) {
            LOG.warn("LOCK PERDIDO | conta={} dono={}", conta.clienteId(), dono);
            throw new TravaIndisponivelException("conta:" + conta.clienteId());
        }
        LOG.info("SAVE + LOCK LIBERADO | conta={} dono={}", conta.clienteId(), dono);
    }

    public void liberar(String clienteId, String dono) {
        Query minhaTrava = new Query(Criteria.where("clienteId").is(clienteId).and("trava.dono").is(dono));
        mongo.updateFirst(minhaTrava, new Update().unset("trava"), ContaDocument.class);
        LOG.info("LOCK LIBERADO SEM ALTERACAO | conta={} dono={}", clienteId, dono);
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
