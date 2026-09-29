package com.orbitapay.clientes.domain.exception;

public class ClienteNaoEncontradoException extends RuntimeException {

    public ClienteNaoEncontradoException(String clienteId) {
        super("Cliente não encontrado: " + clienteId);
    }
}
