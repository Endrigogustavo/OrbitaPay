package com.orbitapay.negociacao.adapter.out.persistence;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.orbitapay.negociacao.adapter.out.persistence.trava.TravaDocument;

@Document("ativos_negociaveis")
public record AtivoNegociavelDocument(
        @Id String ticker,
        String nome,
        String bolsa,
        String moeda,
        BigDecimal cambio,
        BigDecimal cotacao,
        long quantidadeEmitida,
        long quantidadeDisponivel,
        long quantidadeReservada,
        boolean negociavel,
        TravaDocument trava) {
}
