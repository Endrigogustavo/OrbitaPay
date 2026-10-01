package com.orbitapay.pagamentos.application.service;

public class ProvedorIndisponivelException extends RuntimeException {

    public ProvedorIndisponivelException(String provedor, String detalhe) {
        super("Provedor " + provedor + " indisponível: " + detalhe);
    }
}
