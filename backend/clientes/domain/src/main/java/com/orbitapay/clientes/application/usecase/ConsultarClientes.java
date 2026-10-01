package com.orbitapay.clientes.application.usecase;

import java.util.List;

import com.orbitapay.clientes.domain.exception.ClienteNaoEncontradoException;
import com.orbitapay.clientes.domain.model.Cliente;
import com.orbitapay.clientes.domain.repository.ClienteRepository;

public class ConsultarClientes {

    private final ClienteRepository repositorio;

    public ConsultarClientes(ClienteRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Cliente buscar(String clienteId) {
        return repositorio.buscarPorId(clienteId).orElseThrow(() -> new ClienteNaoEncontradoException(clienteId));
    }

    public List<Cliente> listar() {
        return repositorio.listar();
    }
}
