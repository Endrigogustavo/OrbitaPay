package com.orbitapay.ativos.web.dto;

import java.math.BigDecimal;

public record AtivoRequest(String ticker, String nome, String setor, String bolsa, BigDecimal cotacao,
        Long quantidadeEmitida) {
}
