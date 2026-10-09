package com.orbitapay.carteira.application.usecase;

import java.math.BigDecimal;
import java.util.Map;

import com.orbitapay.carteira.domain.repository.AtivoCotadoRepository;

public class AtualizarCotacoes {

    private final AtivoCotadoRepository ativos;

    public AtualizarCotacoes(AtivoCotadoRepository ativos) {
        this.ativos = ativos;
    }

    public void executar(Map<String, BigDecimal> cotacoes) {
        ativos.atualizarCotacoes(cotacoes);
    }
}
