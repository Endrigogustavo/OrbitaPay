package com.orbitapay.carteira.domain.exception;

public class AcoesInsuficientesException extends RuntimeException {

    public AcoesInsuficientesException(String mensagem) {
        super(mensagem);
    }
}
