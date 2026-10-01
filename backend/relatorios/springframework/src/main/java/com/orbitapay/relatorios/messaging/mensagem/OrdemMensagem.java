package com.orbitapay.relatorios.messaging.mensagem;

import java.math.BigDecimal;
import java.time.Instant;

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
        BigDecimal valorTotal,
        String motivo,
        Instant enviadoEm) {
}
