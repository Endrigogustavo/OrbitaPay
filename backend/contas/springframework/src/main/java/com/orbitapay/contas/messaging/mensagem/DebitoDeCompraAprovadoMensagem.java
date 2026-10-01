package com.orbitapay.contas.messaging.mensagem;

import java.math.BigDecimal;
import java.time.Instant;

public record DebitoDeCompraAprovadoMensagem(
        String eventoId,
        String evento,
        String ordemId,
        String clienteId,
        BigDecimal valor,
        BigDecimal saldoRestante,
        String mensagem,
        Instant enviadoEm) {
}
