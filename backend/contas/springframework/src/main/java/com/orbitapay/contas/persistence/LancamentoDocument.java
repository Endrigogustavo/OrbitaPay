package com.orbitapay.contas.persistence;

import java.math.BigDecimal;
import java.time.Instant;

public record LancamentoDocument(String id, String tipo, BigDecimal valor, String descricao, String referencia,
        Instant ocorridoEm) {
}
