package com.orbitapay.negociacao.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.orbitapay.negociacao.domain.model.Ordem;

public record OrdemResponse(
        String id,
        String ticker,
        String tipo,
        int quantidade,
        BigDecimal precoUnitario,
        String moeda,
        BigDecimal cambio,
        BigDecimal valorTotal,
        String status,
        String motivoRejeicao,
        Instant criadaEm,
        Instant atualizadaEm) {

    public static OrdemResponse de(Ordem ordem) {
        return new OrdemResponse(ordem.id(), ordem.ticker(), ordem.tipo().name(), ordem.quantidade(),
                ordem.precoUnitario(), ordem.moeda(), ordem.cambio(), ordem.valorTotal(), ordem.status().name(),
                ordem.motivoRejeicao(), ordem.criadaEm(), ordem.atualizadaEm());
    }
}
