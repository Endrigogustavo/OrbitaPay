package com.orbitapay.relatorios.web;

public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException() {
        super("Acesso restrito ao gerente");
    }
}
