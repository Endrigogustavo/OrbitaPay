package com.orbitapay.negociacao.application.usecase;

import com.orbitapay.negociacao.application.port.in.SincronizarInvestidoresUseCase;
import com.orbitapay.negociacao.application.port.out.InvestidorRepository;
import com.orbitapay.negociacao.domain.model.Investidor;

public class SincronizarInvestidores implements SincronizarInvestidoresUseCase {

    private final InvestidorRepository investidores;

    public SincronizarInvestidores(InvestidorRepository investidores) {
        this.investidores = investidores;
    }

    @Override
    public void registrar(String clienteId, boolean bloqueado) {
        Investidor investidor = investidores.buscar(clienteId).orElseGet(() -> new Investidor(clienteId, bloqueado));
        investidor.alterarSituacao(bloqueado);
        investidores.salvar(investidor);
    }

    @Override
    public void remover(String clienteId) {
        investidores.remover(clienteId);
    }
}
