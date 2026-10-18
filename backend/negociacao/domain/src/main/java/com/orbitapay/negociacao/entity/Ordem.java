package com.orbitapay.negociacao.entity;

import java.math.BigDecimal;
import java.time.Instant;

public record Ordem(String id, String clienteId, String ticker, TipoOrdem tipo, int quantidade,
        BigDecimal precoUnitario, String moeda, BigDecimal cambio, BigDecimal valorTotal, StatusOrdem status,
        String motivoRejeicao, Instant criadaEm, Instant atualizadaEm) {
}
