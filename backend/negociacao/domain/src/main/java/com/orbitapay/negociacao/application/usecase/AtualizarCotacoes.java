package com.orbitapay.negociacao.application.usecase;

import java.math.BigDecimal;
import java.util.Map;

import com.orbitapay.negociacao.domain.repository.AtivoNegociavelRepository;

public class AtualizarCotacoes {

    private final AtivoNegociavelRepository ativos;

    public AtualizarCotacoes(AtivoNegociavelRepository ativos) {
        this.ativos = ativos;
    }

    public void executar(Map<String, BigDecimal> cotacoes) {
        ativos.atualizarCotacoes(cotacoes);
    }
}
