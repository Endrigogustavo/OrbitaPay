package com.orbitapay.ativos.domain.exception;

public class AtivoNaoEncontradoException extends RuntimeException {

    public AtivoNaoEncontradoException(String ticker) {
        super("Ativo não encontrado: " + ticker);
    }
}
