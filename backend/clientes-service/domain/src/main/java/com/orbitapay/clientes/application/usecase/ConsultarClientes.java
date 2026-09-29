package com.orbitapay.clientes.application.usecase;

import java.util.List;

import com.orbitapay.clientes.application.port.in.ConsultarClientesUseCase;
import com.orbitapay.clientes.application.port.out.ClienteRepository;
import com.orbitapay.clientes.domain.exception.ClienteNaoEncontradoException;
import com.orbitapay.clientes.domain.model.Cliente;

public class ConsultarClientes implements ConsultarClientesUseCase {

    private final ClienteRepository repositorio;

    public ConsultarClientes(ClienteRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Cliente buscar(String clienteId) {
        return repositorio.buscarPorId(clienteId).orElseThrow(() -> new ClienteNaoEncontradoException(clienteId));
    }

    @Override
    public List<Cliente> listar() {
        return repositorio.listar();
    }
}
