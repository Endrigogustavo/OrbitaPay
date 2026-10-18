package com.orbitapay.negociacao.infrastructure.transaction;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import com.mongodb.MongoException;
import com.orbitapay.negociacao.application.gateway.ITransacaoGateway;

@Component
public class TransacaoMongoGateway implements ITransacaoGateway {

    private static final int MAXIMO_DE_TENTATIVAS = 50;

    private final TransactionTemplate modeloDeTransacao;

    public TransacaoMongoGateway(TransactionTemplate modeloDeTransacao) {
        this.modeloDeTransacao = modeloDeTransacao;
    }

    @Override
    public void executarEmTransacao(Runnable operacao) {
        executarEmTransacaoERetornar(() -> {
            operacao.run();
            return null;
        });
    }

    @Override
    public <T> T executarEmTransacaoERetornar(Supplier<T> operacao) {
        for (int tentativa = 1; ; tentativa++) {
            try {
                return modeloDeTransacao.execute(estado -> operacao.get());
            } catch (RuntimeException erro) {
                if (!foiConflitoComOutraTransacao(erro) || tentativa == MAXIMO_DE_TENTATIVAS) {
                    throw erro;
                }
                esperarUmPoucoAntesDeTentarDeNovo();
            }
        }
    }

    private static boolean foiConflitoComOutraTransacao(Throwable erro) {
        for (Throwable causa = erro; causa != null; causa = causa.getCause()) {
            if (causa instanceof MongoException erroDoMongo
                    && (erroDoMongo.hasErrorLabel(MongoException.TRANSIENT_TRANSACTION_ERROR_LABEL)
                            || erroDoMongo.hasErrorLabel(MongoException.UNKNOWN_TRANSACTION_COMMIT_RESULT_LABEL))) {
                return true;
            }
        }
        return false;
    }

    private static void esperarUmPoucoAntesDeTentarDeNovo() {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextLong(5, 40));
        } catch (InterruptedException interrompido) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Transação interrompida", interrompido);
        }
    }
}
