package com.orbitapay.ativos.entity;

import java.math.BigDecimal;

public record Bolsa(String codigo, String nome, String cidade, String moeda, BigDecimal cambio, String fuso,
        String abertura, String fechamento) {
}
