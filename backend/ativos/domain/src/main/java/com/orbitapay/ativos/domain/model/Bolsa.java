package com.orbitapay.ativos.domain.model;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Objects;

public record Bolsa(String codigo, String nome, String cidade, String moeda, BigDecimal cambio, String fuso,
        LocalTime abertura, LocalTime fechamento) {

    public Bolsa {
        Objects.requireNonNull(codigo);
        Objects.requireNonNull(nome);
        Objects.requireNonNull(moeda);
        Objects.requireNonNull(cambio);
        Objects.requireNonNull(fuso);
        Objects.requireNonNull(abertura);
        Objects.requireNonNull(fechamento);
    }
}
