package com.orbitapay.contas.domain.repository;

import java.util.List;
import java.util.Optional;

import com.orbitapay.contas.domain.model.Conta;

public interface ContaRepository {

    String proximoId();

    boolean existePorCliente(String clienteId);

    Optional<Conta> buscarPorCliente(String clienteId);

    List<Conta> listar();

    void inserir(Conta conta);

    void removerPorCliente(String clienteId);
}
