package com.orbitapay.negociacao.exception;

public class ClienteBloqueadoException extends RuntimeException {

    public ClienteBloqueadoException() {
        super("Conta bloqueada: ordens na bolsa estão suspensas");
    }
}
