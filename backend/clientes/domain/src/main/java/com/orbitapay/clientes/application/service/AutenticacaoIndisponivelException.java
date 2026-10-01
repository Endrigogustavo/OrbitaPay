package com.orbitapay.clientes.application.service;

public class AutenticacaoIndisponivelException extends RuntimeException {

    public AutenticacaoIndisponivelException() {
        super("Não foi possível criar seu acesso agora. Tente novamente em instantes");
    }
}
