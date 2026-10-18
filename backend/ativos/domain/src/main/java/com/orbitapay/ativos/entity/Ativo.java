package com.orbitapay.ativos.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record Ativo(String ticker, String nome, String setor, String bolsa, BigDecimal cotacao,
        List<BigDecimal> historico, long quantidadeEmitida, long quantidadeDisponivel, Instant listadoEm) {
}
