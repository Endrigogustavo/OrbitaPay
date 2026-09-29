package com.orbitapay.carteira.application.port.in;

import java.math.BigDecimal;
import java.util.Map;

import com.orbitapay.carteira.domain.model.AtivoCotado;

public interface SincronizarCotacoesUseCase {

    void registrar(AtivoCotado ativo);

    void remover(String ticker);

    void atualizarCotacoes(Map<String, BigDecimal> cotacoes);
}
