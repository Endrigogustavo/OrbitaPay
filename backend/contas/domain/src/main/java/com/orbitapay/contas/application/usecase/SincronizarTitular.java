package com.orbitapay.contas.application.usecase;

import com.orbitapay.contas.domain.repository.ContaRepository;
import com.orbitapay.contas.domain.repository.ContaTravada;

public class SincronizarTitular {

    private final ContaRepository repositorio;

    public SincronizarTitular(ContaRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void atualizarNome(String clienteId, String nome) {
        if (!repositorio.existePorCliente(clienteId)) {
            return;
        }
        ContaTravada travada = repositorio.travarPorCliente(clienteId);
        try {
            travada.conta().atualizarTitular(nome);
            repositorio.salvarELiberar(travada);
        } catch (RuntimeException erro) {
            repositorio.liberar(travada);
            throw erro;
        }
    }

    public void alterarSituacao(String clienteId, boolean bloqueado) {
        if (!repositorio.existePorCliente(clienteId)) {
            return;
        }
        ContaTravada travada = repositorio.travarPorCliente(clienteId);
        try {
            travada.conta().alterarSituacaoDoTitular(bloqueado);
            repositorio.salvarELiberar(travada);
        } catch (RuntimeException erro) {
            repositorio.liberar(travada);
            throw erro;
        }
    }

    public void encerrar(String clienteId) {
        repositorio.removerPorCliente(clienteId);
    }
}
