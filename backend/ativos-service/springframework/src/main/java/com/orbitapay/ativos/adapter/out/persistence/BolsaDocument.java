package com.orbitapay.ativos.adapter.out.persistence;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("bolsas")
public record BolsaDocument(
        @Id String codigo,
        String nome,
        String cidade,
        String moeda,
        BigDecimal cambio,
        String fuso,
        String abertura,
        String fechamento) {
}
