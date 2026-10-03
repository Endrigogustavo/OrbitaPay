package com.orbitapay.negociacao.application.usecase;

import com.orbitapay.negociacao.domain.model.Investidor;
import com.orbitapay.negociacao.domain.repository.InvestidorRepository;

public class SincronizarInvestidores {

    private final InvestidorRepository investidores;

    public SincronizarInvestidores(InvestidorRepository investidores) {
        this.investidores = investidores;
    }

    public void registrar(String clienteId, boolean bloqueado) {
        investidores.salvar(new Investidor(clienteId, bloqueado));
    }

    public void remover(String clienteId) {
        investidores.remover(clienteId);
    }
}
