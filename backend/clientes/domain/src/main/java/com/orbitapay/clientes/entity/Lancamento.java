package com.orbitapay.clientes.entity;

import java.math.BigDecimal;
import java.time.Instant;

public record Lancamento(String id, TipoLancamento tipo, BigDecimal valor, String descricao, String referencia,
        Instant ocorridoEm) {
}
