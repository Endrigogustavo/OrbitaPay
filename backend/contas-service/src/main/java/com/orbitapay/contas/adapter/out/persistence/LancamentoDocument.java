package com.orbitapay.contas.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.Instant;

public record LancamentoDocument(String id, String tipo, BigDecimal valor, String descricao, String ordemId,
        Instant ocorridoEm) {
}
