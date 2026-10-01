package com.orbitapay.auth.application.usecase;

import com.orbitapay.auth.domain.exception.CredencialNaoEncontradaException;
import com.orbitapay.auth.domain.model.Credencial;
import com.orbitapay.auth.domain.repository.CredencialRepository;

public class ConsultarCredencial {

    private final CredencialRepository repositorio;

    public ConsultarCredencial(CredencialRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Credencial doCliente(String clienteId) {
        return repositorio.buscarPorCliente(clienteId)
                .orElseThrow(() -> new CredencialNaoEncontradaException(clienteId));
    }
}
