package com.orbitapay.negociacao.application.usecase;

import com.orbitapay.negociacao.domain.repository.InvestidorRepository;

public class RemoverInvestidor {

    private final InvestidorRepository investidores;

    public RemoverInvestidor(InvestidorRepository investidores) {
        this.investidores = investidores;
    }

    public void executar(String clienteId) {
        investidores.remover(clienteId);
    }
}
