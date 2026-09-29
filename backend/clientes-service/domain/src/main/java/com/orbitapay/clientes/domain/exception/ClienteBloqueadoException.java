package com.orbitapay.clientes.domain.exception;

public class ClienteBloqueadoException extends RuntimeException {

    public ClienteBloqueadoException() {
        super("Conta bloqueada: saques e ordens estão suspensos");
    }
}
