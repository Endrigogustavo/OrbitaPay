package com.orbitapay.negociacao.application.exception;

public class OperacaoRecusadaException extends RuntimeException {

    public OperacaoRecusadaException(String motivo) {
        super(motivo);
    }
}
