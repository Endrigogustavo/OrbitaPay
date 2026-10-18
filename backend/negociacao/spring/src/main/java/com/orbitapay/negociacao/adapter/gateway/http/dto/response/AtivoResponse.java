package com.orbitapay.negociacao.adapter.gateway.http.dto.response;

import java.math.BigDecimal;

public record AtivoResponse(String ticker, String nome, String moeda, BigDecimal cambio, BigDecimal cotacao) {
}
