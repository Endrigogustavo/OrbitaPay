package com.orbitapay.negociacao.entity;

import java.math.BigDecimal;

public record PosicaoValorizada(String ticker, String nome, long quantidade, BigDecimal precoMedio,
        BigDecimal cotacao, String moeda, BigDecimal valorDeMercado, BigDecimal custo,
        BigDecimal resultadoPercentual) {
}
