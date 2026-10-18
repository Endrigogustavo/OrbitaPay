package com.orbitapay.clientes.application.gateway;

import java.util.List;
import java.util.Optional;

import com.orbitapay.clientes.entity.Conta;

public interface IContaGateway {

    void salvar(Conta conta);

    Optional<Conta> buscarPorCliente(String clienteId);

    List<Conta> listarTodas();

    void removerPorCliente(String clienteId);
}
