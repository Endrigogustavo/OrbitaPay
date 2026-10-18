package com.orbitapay.clientes.adapter.gateway.exception;

public class ServicoIndisponivelException extends RuntimeException {

    public ServicoIndisponivelException(String servico, Throwable causa) {
        super("Serviço " + servico + " indisponível. Tente de novo em instantes", causa);
    }
}
