package com.orbitapay.negociacao.application.dto;

import java.math.BigDecimal;

public record AtivoDoCatalogo(String ticker, String nome, String bolsa, String moeda, BigDecimal cambio,
        BigDecimal cotacao, long quantidadeEmitida) {
}
