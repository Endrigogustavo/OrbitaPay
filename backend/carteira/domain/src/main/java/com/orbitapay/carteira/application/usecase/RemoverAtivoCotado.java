package com.orbitapay.carteira.application.usecase;

import com.orbitapay.carteira.domain.repository.AtivoCotadoRepository;

public class RemoverAtivoCotado {

    private final AtivoCotadoRepository ativos;

    public RemoverAtivoCotado(AtivoCotadoRepository ativos) {
        this.ativos = ativos;
    }

    public void executar(String ticker) {
        ativos.remover(ticker);
    }
}
