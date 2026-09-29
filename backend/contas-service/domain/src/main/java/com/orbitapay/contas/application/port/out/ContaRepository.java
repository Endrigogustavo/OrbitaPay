package com.orbitapay.contas.application.port.out;

import java.util.List;
import java.util.Optional;

import com.orbitapay.contas.domain.model.Conta;

public interface ContaRepository {

    String proximoId();

    boolean existePorCliente(String clienteId);

    Optional<Conta> buscarPorCliente(String clienteId);

    List<Conta> listar();

    void inserir(Conta conta);

    ContaTravada travarPorCliente(String clienteId);

    void salvarELiberar(ContaTravada contaTravada);

    void liberar(ContaTravada contaTravada);

    void removerPorCliente(String clienteId);
}
