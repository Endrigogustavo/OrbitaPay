package com.orbitapay.clientes.application.gateway;

import java.util.List;
import java.util.Optional;

import com.orbitapay.clientes.entity.Cliente;

public interface IClienteGateway {

    void salvar(Cliente cliente);

    Optional<Cliente> buscarPorId(String clienteId);

    List<Cliente> listarTodos();

    boolean emailEmUsoPorOutroCliente(String email, String clienteId);

    void remover(String clienteId);
}
