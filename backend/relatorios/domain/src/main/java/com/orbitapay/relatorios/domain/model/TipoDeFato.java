package com.orbitapay.relatorios.domain.model;

public enum TipoDeFato {
    CLIENTE_CADASTRADO,
    CLIENTE_BLOQUEADO,
    CLIENTE_DESBLOQUEADO,
    CLIENTE_REMOVIDO,
    COMPRA_EXECUTADA,
    VENDA_EXECUTADA,
    ORDEM_REJEITADA,
    DEPOSITO_CONFIRMADO,
    DEPOSITO_EXPIRADO;

    public boolean negociacao() {
        return this == COMPRA_EXECUTADA || this == VENDA_EXECUTADA;
    }
}
