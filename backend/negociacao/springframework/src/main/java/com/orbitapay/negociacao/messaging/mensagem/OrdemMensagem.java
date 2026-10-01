package com.orbitapay.negociacao.messaging.mensagem;

import java.math.BigDecimal;
import java.time.Instant;

public record OrdemMensagem(
        String eventoId,
        String evento,
        String ordemId,
        String clienteId,
        String ticker,
        String tipo,
        int quantidade,
        BigDecimal precoUnitario,
        String moeda,
        BigDecimal valorTotal,
        String status,
        String motivo,
        String mensagem,
        Instant enviadoEm) {
}
