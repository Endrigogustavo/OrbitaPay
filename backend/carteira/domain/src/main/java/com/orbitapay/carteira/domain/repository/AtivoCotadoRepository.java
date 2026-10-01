package com.orbitapay.carteira.domain.repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

import com.orbitapay.carteira.domain.model.AtivoCotado;

public interface AtivoCotadoRepository {

    Map<String, AtivoCotado> buscarTodos(Collection<String> tickers);

    void salvar(AtivoCotado ativo);

    void remover(String ticker);

    void atualizarCotacoes(Map<String, BigDecimal> cotacoes);
}
