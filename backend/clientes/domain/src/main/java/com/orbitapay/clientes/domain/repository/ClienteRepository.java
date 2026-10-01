package com.orbitapay.clientes.domain.repository;

import java.util.List;
import java.util.Optional;

import com.orbitapay.clientes.domain.model.Cliente;
import com.orbitapay.clientes.domain.model.Email;

public interface ClienteRepository {

    String proximoId();

    Optional<Cliente> buscarPorId(String clienteId);

    Optional<Cliente> buscarPorEmail(Email email);

    boolean existeEmail(Email email);

    boolean existeEmailDeOutroCliente(Email email, String clienteId);

    List<Cliente> listar();

    boolean vazio();

    void salvar(Cliente cliente);

    void remover(String clienteId);
}
