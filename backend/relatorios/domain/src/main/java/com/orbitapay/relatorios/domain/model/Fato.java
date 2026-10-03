package com.orbitapay.relatorios.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record Fato(
        String eventoId,
        TipoDeFato tipo,
        String clienteId,
        String ticker,
        long quantidade,
        BigDecimal valor,
        String detalhe,
        Instant ocorridoEm) {

    public Fato {
        valor = valor == null ? BigDecimal.ZERO : valor;
    }
}
