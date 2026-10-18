package com.orbitapay.clientes.adapter.gateway.mongo.document;

import java.math.BigDecimal;
import java.time.Instant;

public record LancamentoDocument(String id, String tipo, BigDecimal valor, String descricao, String referencia,
        Instant ocorridoEm) {
}
