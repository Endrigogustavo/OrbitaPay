package com.orbitapay.negociacao.messaging.mensagem;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventoDeAtivoMensagem(
        String eventoId,
        String evento,
        String ticker,
        String nome,
        String bolsa,
        String moeda,
        BigDecimal cambio,
        BigDecimal cotacao,
        long quantidadeEmitida,
        List<Cotacao> cotacoes) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Cotacao(String ticker, BigDecimal cotacao) {
    }
}
