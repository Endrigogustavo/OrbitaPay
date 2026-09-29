package com.orbitapay.clientes.domain.exception;

public class EmailJaCadastradoException extends RegraDeNegocioException {

    public EmailJaCadastradoException() {
        super("Este e-mail já tem conta");
    }
}
