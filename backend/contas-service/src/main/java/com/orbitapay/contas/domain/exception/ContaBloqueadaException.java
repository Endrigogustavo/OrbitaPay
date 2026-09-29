package com.orbitapay.contas.domain.exception;

public class ContaBloqueadaException extends RuntimeException {

    public ContaBloqueadaException() {
        super("Conta bloqueada: saques e ordens estão suspensos");
    }
}
