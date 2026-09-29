package com.orbitapay.negociacao.domain.exception;

public class InvestidorBloqueadoException extends RuntimeException {

    public InvestidorBloqueadoException() {
        super("Conta bloqueada: ordens na bolsa estão suspensas");
    }
}
