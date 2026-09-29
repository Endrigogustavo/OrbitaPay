package com.orbitapay.clientes.adapter.in.web;

public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException() {
        super("Acesso restrito ao gerente");
    }
}
