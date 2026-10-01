package com.orbitapay.negociacao.messaging.mensagem;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AtivoConsultadoMensagem(
        boolean encontrado,
        String ticker,
        String nome,
        String bolsa,
        String moeda,
        BigDecimal cambio,
        BigDecimal cotacao,
        long quantidadeEmitida) {
}
