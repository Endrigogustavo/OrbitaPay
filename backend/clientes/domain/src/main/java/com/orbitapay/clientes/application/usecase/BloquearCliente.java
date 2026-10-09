package com.orbitapay.clientes.application.usecase;

import java.time.Instant;

import com.orbitapay.clientes.application.service.PublicadorDeEventosDeCliente;
import com.orbitapay.clientes.domain.event.ClienteSituacaoAlterada;
import com.orbitapay.clientes.domain.exception.ClienteNaoEncontradoException;
import com.orbitapay.clientes.domain.model.Cliente;
import com.orbitapay.clientes.domain.model.MotivoBloqueio;
import com.orbitapay.clientes.domain.repository.ClienteRepository;

public class BloquearCliente {

    private final ClienteRepository repositorio;
    private final PublicadorDeEventosDeCliente publicador;

    public BloquearCliente(ClienteRepository repositorio, PublicadorDeEventosDeCliente publicador) {
        this.repositorio = repositorio;
        this.publicador = publicador;
    }

    public Cliente executar(String clienteId, MotivoBloqueio motivo) {
        Cliente cliente = repositorio.buscarPorId(clienteId)
                .orElseThrow(() -> new ClienteNaoEncontradoException(clienteId));
        cliente.bloquear(motivo);
        repositorio.salvar(cliente);
        publicador.publicar(new ClienteSituacaoAlterada(cliente.id(), true, motivo, Instant.now()));
        return cliente;
    }
}
