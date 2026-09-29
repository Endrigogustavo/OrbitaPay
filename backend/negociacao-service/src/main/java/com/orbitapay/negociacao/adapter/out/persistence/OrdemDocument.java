package com.orbitapay.negociacao.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("ordens")
public record OrdemDocument(
        @Id String id,
        @Indexed String clienteId,
        String ticker,
        String tipo,
        int quantidade,
        BigDecimal precoUnitario,
        String moeda,
        BigDecimal cambio,
        BigDecimal valorTotal,
        String status,
        String motivoRejeicao,
        Instant criadaEm,
        Instant atualizadaEm) {
}
