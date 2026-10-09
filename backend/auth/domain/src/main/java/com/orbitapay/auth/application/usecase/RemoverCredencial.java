package com.orbitapay.auth.application.usecase;

import com.orbitapay.auth.domain.repository.CredencialRepository;

public class RemoverCredencial {

    private final CredencialRepository repositorio;

    public RemoverCredencial(CredencialRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void executar(String clienteId) {
        repositorio.remover(clienteId);
    }
}
