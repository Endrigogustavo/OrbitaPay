package com.orbitapay.clientes.exception;

public class ContaBloqueadaException extends RuntimeException {

    public ContaBloqueadaException() {
        super("Conta bloqueada: movimentações suspensas");
    }
}
