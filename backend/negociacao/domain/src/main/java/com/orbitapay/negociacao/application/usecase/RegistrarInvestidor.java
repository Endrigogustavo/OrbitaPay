package com.orbitapay.negociacao.application.usecase;

import com.orbitapay.negociacao.domain.model.Investidor;
import com.orbitapay.negociacao.domain.repository.InvestidorRepository;

public class RegistrarInvestidor {

    private final InvestidorRepository investidores;

    public RegistrarInvestidor(InvestidorRepository investidores) {
        this.investidores = investidores;
    }

    public void executar(String clienteId, boolean bloqueado) {
        investidores.salvar(new Investidor(clienteId, bloqueado));
    }
}
