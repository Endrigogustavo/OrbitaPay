package com.orbitapay.auth.application.usecase;

import com.orbitapay.auth.domain.model.Email;
import com.orbitapay.auth.domain.repository.CredencialRepository;

/** Mantém a credencial alinhada ao que o contexto de Clientes publica (e-mail, desbloqueio e encerramento). */
public class SincronizarCliente {

    private final CredencialRepository repositorio;

    public SincronizarCliente(CredencialRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void atualizarEmail(String clienteId, String email) {
        repositorio.buscarPorCliente(clienteId).ifPresent(credencial -> {
            credencial.alterarEmail(new Email(email));
            repositorio.salvar(credencial);
        });
    }

    public void desbloqueado(String clienteId) {
        repositorio.buscarPorCliente(clienteId).ifPresent(credencial -> {
            credencial.zerarTentativas();
            repositorio.salvar(credencial);
        });
    }

    public void encerrar(String clienteId) {
        repositorio.remover(clienteId);
    }
}
