package com.orbitapay.carteira.application.usecase;

import com.orbitapay.carteira.domain.model.AtivoCotado;
import com.orbitapay.carteira.domain.repository.AtivoCotadoRepository;

public class RegistrarAtivoCotado {

    private final AtivoCotadoRepository ativos;

    public RegistrarAtivoCotado(AtivoCotadoRepository ativos) {
        this.ativos = ativos;
    }

    public void executar(AtivoCotado ativo) {
        ativos.salvar(ativo);
    }
}
