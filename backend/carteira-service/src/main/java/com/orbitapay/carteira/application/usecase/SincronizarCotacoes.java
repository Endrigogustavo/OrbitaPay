package com.orbitapay.carteira.application.usecase;

import java.math.BigDecimal;
import java.util.Map;

import com.orbitapay.carteira.application.port.in.SincronizarCotacoesUseCase;
import com.orbitapay.carteira.application.port.out.AtivoCotadoRepository;
import com.orbitapay.carteira.domain.model.AtivoCotado;

public class SincronizarCotacoes implements SincronizarCotacoesUseCase {

    private final AtivoCotadoRepository ativos;

    public SincronizarCotacoes(AtivoCotadoRepository ativos) {
        this.ativos = ativos;
    }

    @Override
    public void registrar(AtivoCotado ativo) {
        ativos.salvar(ativo);
    }

    @Override
    public void remover(String ticker) {
        ativos.remover(ticker);
    }

    @Override
    public void atualizarCotacoes(Map<String, BigDecimal> cotacoes) {
        ativos.atualizarCotacoes(cotacoes);
    }
}
