package com.orbitapay.ativos.application.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SimuladorDeMercado {

    Optional<BigDecimal> proximaCotacao(BigDecimal cotacaoAtual);

    List<BigDecimal> historicoRetroativo(BigDecimal cotacaoAtual, int pontos);
}
