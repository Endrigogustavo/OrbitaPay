package com.orbitapay.clientes.web;

public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException() {
        super("Acesso restrito ao gerente");
    }
}
