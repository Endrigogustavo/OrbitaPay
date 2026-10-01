package com.orbitapay.carteira.application.exception;

public class TravaIndisponivelException extends RuntimeException {

    public TravaIndisponivelException(String recurso) {
        super("Recurso ocupado por outra operação, tente novamente: " + recurso);
    }
}
