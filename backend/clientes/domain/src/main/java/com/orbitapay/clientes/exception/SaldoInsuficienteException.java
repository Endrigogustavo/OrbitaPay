package com.orbitapay.clientes.exception;

public class SaldoInsuficienteException extends RuntimeException {

    public SaldoInsuficienteException(String saldoDisponivelFormatado) {
        super("Saldo insuficiente · disponível " + saldoDisponivelFormatado);
    }
}
