package com.orbitapay.negociacao.web.dto;

public record OrdemRequest(String ticker, String tipo, Integer quantidade) {
}
