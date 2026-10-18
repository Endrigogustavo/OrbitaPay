package com.orbitapay.ativos.adapter.controller.dto.response;

import java.math.BigDecimal;

import com.orbitapay.ativos.entity.Bolsa;

public record BolsaResponse(String codigo, String nome, String cidade, String moeda, BigDecimal cambio, String fuso,
        String abertura, String fechamento) {

    public static BolsaResponse criarAPartirDe(Bolsa bolsa) {
        return new BolsaResponse(bolsa.codigo(), bolsa.nome(), bolsa.cidade(), bolsa.moeda(), bolsa.cambio(),
                bolsa.fuso(), bolsa.abertura(), bolsa.fechamento());
    }
}
