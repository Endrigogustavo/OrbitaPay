package com.orbitapay.negociacao.application.usecase;

import com.orbitapay.negociacao.domain.model.Investidor;
import com.orbitapay.negociacao.domain.repository.InvestidorRepository;

public class SincronizarInvestidores {

    private final InvestidorRepository investidores;

    public SincronizarInvestidores(InvestidorRepository investidores) {
        this.investidores = investidores;
    }

    public void registrar(String clienteId, boolean bloqueado) {
        Investidor investidor = investidores.buscar(clienteId).orElseGet(() -> new Investidor(clienteId, bloqueado));
        investidor.alterarSituacao(bloqueado);
        investidores.salvar(investidor);
    }

    public void remover(String clienteId) {
        investidores.remover(clienteId);
    }
}
