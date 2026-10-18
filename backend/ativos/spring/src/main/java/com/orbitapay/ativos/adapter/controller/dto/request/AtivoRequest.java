package com.orbitapay.ativos.adapter.controller.dto.request;

import java.math.BigDecimal;

public record AtivoRequest(String ticker, String nome, String setor, String bolsa, BigDecimal cotacao,
        Long quantidadeEmitida) {
}
