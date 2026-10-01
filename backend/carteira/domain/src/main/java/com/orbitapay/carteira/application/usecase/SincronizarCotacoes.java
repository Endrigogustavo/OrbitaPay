package com.orbitapay.carteira.application.usecase;

import java.math.BigDecimal;
import java.util.Map;

import com.orbitapay.carteira.domain.model.AtivoCotado;
import com.orbitapay.carteira.domain.repository.AtivoCotadoRepository;

public class SincronizarCotacoes {

    private final AtivoCotadoRepository ativos;

    public SincronizarCotacoes(AtivoCotadoRepository ativos) {
        this.ativos = ativos;
    }

    public void registrar(AtivoCotado ativo) {
        ativos.salvar(ativo);
    }

    public void remover(String ticker) {
        ativos.remover(ticker);
    }

    public void atualizarCotacoes(Map<String, BigDecimal> cotacoes) {
        ativos.atualizarCotacoes(cotacoes);
    }
}
