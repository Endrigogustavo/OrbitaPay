package com.orbitapay.ativos.adapter.controller.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.orbitapay.ativos.entity.Ativo;
import com.orbitapay.ativos.entity.AtivoComBolsa;

public record AtivoResponse(String ticker, String nome, String setor, String bolsa, String moeda, BigDecimal cambio,
        BigDecimal cotacao, BigDecimal abertura, BigDecimal variacaoPercentual, List<BigDecimal> historico,
        long quantidadeEmitida, long quantidadeDisponivel) {

    public static AtivoResponse criarAPartirDe(AtivoComBolsa ativoComBolsa) {
        Ativo ativo = ativoComBolsa.ativo();
        BigDecimal abertura = ativo.historico().isEmpty() ? ativo.cotacao() : ativo.historico().getFirst();
        BigDecimal variacaoPercentual = ativo.cotacao().divide(abertura, 6, RoundingMode.HALF_EVEN)
                .subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_EVEN);
        return new AtivoResponse(ativo.ticker(), ativo.nome(), ativo.setor(), ativo.bolsa(),
                ativoComBolsa.bolsa().moeda(), ativoComBolsa.bolsa().cambio(), ativo.cotacao(), abertura,
                variacaoPercentual, ativo.historico(), ativo.quantidadeEmitida(), ativo.quantidadeDisponivel());
    }
}
