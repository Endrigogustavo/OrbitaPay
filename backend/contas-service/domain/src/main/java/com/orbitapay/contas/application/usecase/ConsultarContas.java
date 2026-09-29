package com.orbitapay.contas.application.usecase;

import java.util.List;

import com.orbitapay.contas.application.port.in.ConsultarContasUseCase;
import com.orbitapay.contas.application.port.out.ContaRepository;
import com.orbitapay.contas.domain.exception.ContaNaoEncontradaException;
import com.orbitapay.contas.domain.model.Conta;

public class ConsultarContas implements ConsultarContasUseCase {

    private final ContaRepository repositorio;

    public ConsultarContas(ContaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Conta porCliente(String clienteId) {
        return repositorio.buscarPorCliente(clienteId).orElseThrow(() -> new ContaNaoEncontradaException(clienteId));
    }

    @Override
    public List<Conta> listar() {
        return repositorio.listar();
    }
}
