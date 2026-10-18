package com.orbitapay.negociacao.entity;

import java.math.BigDecimal;

public record Posicao(String ticker, long quantidade, BigDecimal precoMedio) {
}
