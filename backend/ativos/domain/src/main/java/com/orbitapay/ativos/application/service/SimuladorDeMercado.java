package com.orbitapay.ativos.application.service;

import java.math.BigDecimal;
import java.util.List;

public interface SimuladorDeMercado {

    BigDecimal proximaCotacao(BigDecimal cotacaoAtual);

    List<BigDecimal> historicoRetroativo(BigDecimal cotacaoAtual, int pontos);
}
