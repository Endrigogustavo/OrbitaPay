package com.orbitapay.contas.application.usecase;

import com.orbitapay.contas.application.port.in.SincronizarTitularUseCase;
import com.orbitapay.contas.application.port.out.ContaRepository;

public class SincronizarTitular implements SincronizarTitularUseCase {

    private final ContaRepository repositorio;
    private final OperacaoComTrava operacao;

    public SincronizarTitular(ContaRepository repositorio, OperacaoComTrava operacao) {
        this.repositorio = repositorio;
        this.operacao = operacao;
    }

    @Override
    public void atualizarNome(String clienteId, String nome) {
        if (repositorio.existePorCliente(clienteId)) {
            operacao.executar(clienteId, conta -> {
                conta.atualizarTitular(nome);
                return conta;
            });
        }
    }

    @Override
    public void alterarSituacao(String clienteId, boolean bloqueado) {
        if (repositorio.existePorCliente(clienteId)) {
            operacao.executar(clienteId, conta -> {
                conta.alterarSituacaoDoTitular(bloqueado);
                return conta;
            });
        }
    }

    @Override
    public void encerrar(String clienteId) {
        repositorio.removerPorCliente(clienteId);
    }
}
