package com.orbitapay.pagamentos.entity;

import java.math.BigDecimal;
import java.time.Instant;

public record Pagamento(String id, String clienteId, BigDecimal valor, MetodoDePagamento metodo,
        InstrucoesDePagamento instrucoes, StatusDoPagamento status, BigDecimal valorPago, Instant criadoEm,
        Instant concluidoEm) {
}
