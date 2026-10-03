package com.orbitapay.auth.application.usecase;

import java.util.Optional;

import com.orbitapay.auth.domain.model.Credencial;
import com.orbitapay.auth.domain.model.Email;
import com.orbitapay.auth.domain.repository.CredencialRepository;

public class SincronizarCliente {

    private final CredencialRepository repositorio;

    public SincronizarCliente(CredencialRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void atualizarEmail(String clienteId, String email) {
        Optional<Credencial> credencial = repositorio.buscarPorCliente(clienteId);
        if (credencial.isPresent()) {
            credencial.get().alterarEmail(new Email(email));
            repositorio.salvar(credencial.get());
        }
    }

    public void desbloqueado(String clienteId) {
        Optional<Credencial> credencial = repositorio.buscarPorCliente(clienteId);
        if (credencial.isPresent()) {
            credencial.get().zerarTentativas();
            repositorio.salvar(credencial.get());
        }
    }

    public void encerrar(String clienteId) {
        repositorio.remover(clienteId);
    }
}
