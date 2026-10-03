package com.orbitapay.ativos.domain.model;

import java.math.BigDecimal;
import java.time.LocalTime;

public record Bolsa(String codigo, String nome, String cidade, String moeda, BigDecimal cambio, String fuso,
        LocalTime abertura, LocalTime fechamento) {

}
