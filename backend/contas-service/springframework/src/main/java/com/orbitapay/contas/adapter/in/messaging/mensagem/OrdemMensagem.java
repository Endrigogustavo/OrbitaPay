package com.orbitapay.contas.adapter.in.messaging.mensagem;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrdemMensagem(
        String eventoId,
        String ordemId,
        String clienteId,
        String ticker,
        String tipo,
        int quantidade,
        BigDecimal valorTotal) {

    public boolean venda() {
        return "VENDA".equals(tipo);
    }
}
