package com.orbitapay.auth.domain.exception;

public class CredencialNaoEncontradaException extends RuntimeException {

    public CredencialNaoEncontradaException(String clienteId) {
        super("Credencial não encontrada para o cliente " + clienteId);
    }
}
