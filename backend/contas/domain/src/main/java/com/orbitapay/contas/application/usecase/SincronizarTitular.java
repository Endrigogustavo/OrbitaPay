package com.orbitapay.contas.application.usecase;

import com.orbitapay.contas.domain.repository.ContaRepository;

public class SincronizarTitular {

    private final ContaRepository repositorio;
    private final OperacaoComTrava operacao;

    public SincronizarTitular(ContaRepository repositorio, OperacaoComTrava operacao) {
        this.repositorio = repositorio;
        this.operacao = operacao;
    }

    public void atualizarNome(String clienteId, String nome) {
        if (repositorio.existePorCliente(clienteId)) {
            operacao.executar(clienteId, conta -> {
                conta.atualizarTitular(nome);
                return conta;
            });
        }
    }

    public void alterarSituacao(String clienteId, boolean bloqueado) {
        if (repositorio.existePorCliente(clienteId)) {
            operacao.executar(clienteId, conta -> {
                conta.alterarSituacaoDoTitular(bloqueado);
                return conta;
            });
        }
    }

    public void encerrar(String clienteId) {
        repositorio.removerPorCliente(clienteId);
    }
}
