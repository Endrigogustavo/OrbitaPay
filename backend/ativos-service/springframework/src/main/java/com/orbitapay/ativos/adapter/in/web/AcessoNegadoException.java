package com.orbitapay.ativos.adapter.in.web;

public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException() {
        super("Acesso restrito ao gerente");
    }
}
