package com.orbitapay.contas.application.usecase;

import java.util.List;

import com.orbitapay.contas.domain.exception.ContaNaoEncontradaException;
import com.orbitapay.contas.domain.model.Conta;
import com.orbitapay.contas.domain.repository.ContaRepository;

public class ConsultarContas {

    private final ContaRepository repositorio;

    public ConsultarContas(ContaRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Conta porCliente(String clienteId) {
        return repositorio.buscarPorCliente(clienteId).orElseThrow(() -> new ContaNaoEncontradaException(clienteId));
    }

    public List<Conta> listar() {
        return repositorio.listar();
    }
}
