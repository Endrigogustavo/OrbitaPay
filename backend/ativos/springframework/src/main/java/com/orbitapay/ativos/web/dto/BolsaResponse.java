package com.orbitapay.ativos.web.dto;

import java.math.BigDecimal;

import com.orbitapay.ativos.domain.model.Bolsa;

public record BolsaResponse(String codigo, String nome, String cidade, String moeda, BigDecimal cambio, String fuso,
        String abertura, String fechamento) {

    public static BolsaResponse de(Bolsa bolsa) {
        return new BolsaResponse(bolsa.codigo(), bolsa.nome(), bolsa.cidade(), bolsa.moeda(), bolsa.cambio(),
                bolsa.fuso(), bolsa.abertura().toString(), bolsa.fechamento().toString());
    }
}
