package com.orbitapay.ativos.web.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.orbitapay.ativos.application.dto.AtivoCotado;
import com.orbitapay.ativos.domain.model.Ativo;

public record AtivoResponse(
        String ticker,
        String nome,
        String setor,
        String bolsa,
        String moeda,
        BigDecimal cambio,
        BigDecimal cotacao,
        BigDecimal abertura,
        BigDecimal variacaoPercentual,
        List<BigDecimal> historico,
        long quantidadeEmitida) {

    public static AtivoResponse de(AtivoCotado cotado) {
        Ativo ativo = cotado.ativo();
        BigDecimal variacao = ativo.cotacao().divide(ativo.abertura(), 6, RoundingMode.HALF_EVEN)
                .subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_EVEN);
        return new AtivoResponse(ativo.ticker().valor(), ativo.nome(), ativo.setor(), cotado.bolsa().codigo(),
                cotado.bolsa().moeda(), cotado.bolsa().cambio(), ativo.cotacao(), ativo.abertura(), variacao,
                ativo.historico(), ativo.quantidadeEmitida());
    }
}
