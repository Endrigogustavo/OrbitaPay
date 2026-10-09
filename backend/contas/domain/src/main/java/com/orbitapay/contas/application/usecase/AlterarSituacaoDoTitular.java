package com.orbitapay.contas.application.usecase;

import com.orbitapay.contas.domain.repository.ContaRepository;
import com.orbitapay.contas.domain.repository.ContaTravada;
import com.orbitapay.contas.domain.repository.TravaDeConta;

public class AlterarSituacaoDoTitular {

    private final ContaRepository contas;
    private final TravaDeConta trava;

    public AlterarSituacaoDoTitular(ContaRepository contas, TravaDeConta trava) {
        this.contas = contas;
        this.trava = trava;
    }

    public void executar(String clienteId, boolean bloqueado) {
        if (!contas.existePorCliente(clienteId)) {
            return;
        }
        ContaTravada travada = trava.travar(clienteId);
        try {
            travada.conta().alterarSituacaoDoTitular(bloqueado);
            trava.salvarELiberar(travada);
        } catch (RuntimeException erro) {
            trava.liberar(travada);
            throw erro;
        }
    }
}
