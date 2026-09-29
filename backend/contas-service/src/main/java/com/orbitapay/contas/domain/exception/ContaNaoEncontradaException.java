package com.orbitapay.contas.domain.exception;

public class ContaNaoEncontradaException extends RuntimeException {

    public ContaNaoEncontradaException(String clienteId) {
        super("Conta não encontrada para o cliente " + clienteId);
    }
}
