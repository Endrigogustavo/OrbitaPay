package com.orbitapay.negociacao.web.dto;

import java.math.BigDecimal;

import com.orbitapay.negociacao.domain.model.AtivoNegociavel;

public record OfertaResponse(String ticker, BigDecimal cotacao, String moeda, BigDecimal cambio, long quantidadeEmitida,
        long quantidadeDisponivel, long quantidadeReservada, boolean negociavel) {

    public static OfertaResponse de(AtivoNegociavel ativo) {
        return new OfertaResponse(ativo.ticker(), ativo.cotacao(), ativo.moeda(), ativo.cambio(),
                ativo.quantidadeEmitida(), ativo.quantidadeDisponivel(), ativo.quantidadeReservada(),
                ativo.negociavel());
    }
}
