package com.orbitapay.negociacao.entity;

import java.math.BigDecimal;

public record CotacaoDoAtivo(String ticker, String nome, String moeda, BigDecimal cambio, BigDecimal cotacao) {
}
