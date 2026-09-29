package com.orbitapay.carteira.adapter.in.messaging.mensagem;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrdemMensagem(
        String eventoId,
        String evento,
        String ordemId,
        String clienteId,
        String ticker,
        String tipo,
        int quantidade,
        BigDecimal precoUnitario) {

    public boolean compra() {
        return "COMPRA".equals(tipo);
    }
}
