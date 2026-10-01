package com.orbitapay.relatorios.messaging.mensagem;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PagamentoMensagem(
        String eventoId,
        String evento,
        String pagamentoId,
        String clienteId,
        String metodo,
        BigDecimal valor,
        BigDecimal valorPago,
        Instant enviadoEm) {
}
