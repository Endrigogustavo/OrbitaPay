package com.orbitapay.gateway.application.exception;

public class AutenticacaoIndisponivelException extends RuntimeException {

    public AutenticacaoIndisponivelException(Throwable causa) {
        super("Serviço de autenticação indisponível", causa);
    }
}
