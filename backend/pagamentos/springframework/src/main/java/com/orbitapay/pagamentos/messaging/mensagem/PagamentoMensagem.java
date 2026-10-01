package com.orbitapay.pagamentos.messaging.mensagem;

import java.math.BigDecimal;
import java.time.Instant;

public record PagamentoMensagem(
        String eventoId,
        String evento,
        String pagamentoId,
        String clienteId,
        String metodo,
        BigDecimal valor,
        BigDecimal valorPago,
        String provedor,
        Instant concluidoEm,
        String mensagem,
        Instant enviadoEm) {
}
