package com.orbitapay.clientes.application.usecase;

import java.time.Instant;

import com.orbitapay.clientes.application.service.PublicadorDeEventosDeCliente;
import com.orbitapay.clientes.domain.event.ClienteRemovido;
import com.orbitapay.clientes.domain.exception.ClienteNaoEncontradoException;
import com.orbitapay.clientes.domain.repository.ClienteRepository;

public class RemoverCliente {

    private final ClienteRepository repositorio;
    private final PublicadorDeEventosDeCliente publicador;

    public RemoverCliente(ClienteRepository repositorio, PublicadorDeEventosDeCliente publicador) {
        this.repositorio = repositorio;
        this.publicador = publicador;
    }

    public void executar(String clienteId) {
        if (repositorio.buscarPorId(clienteId).isEmpty()) {
            throw new ClienteNaoEncontradoException(clienteId);
        }
        repositorio.remover(clienteId);
        publicador.publicar(new ClienteRemovido(clienteId, Instant.now()));
    }
}
