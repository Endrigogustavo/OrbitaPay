package com.orbitapay.contas.application.usecase;

import com.orbitapay.contas.domain.repository.ContaRepository;

public class EncerrarConta {

    private final ContaRepository contas;

    public EncerrarConta(ContaRepository contas) {
        this.contas = contas;
    }

    public void executar(String clienteId) {
        contas.removerPorCliente(clienteId);
    }
}
