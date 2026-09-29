package com.orbitapay.ativos.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("ativos")
public record AtivoDocument(
        @Id String ticker,
        String nome,
        String setor,
        String bolsa,
        BigDecimal cotacao,
        List<BigDecimal> historico,
        long quantidadeEmitida,
        Instant listadoEm) {
}
