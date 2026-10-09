package com.orbitapay.clientes.application.usecase;

import java.time.Instant;

import com.orbitapay.clientes.application.service.PublicadorDeEventosDeCliente;
import com.orbitapay.clientes.domain.event.ClienteSituacaoAlterada;
import com.orbitapay.clientes.domain.exception.ClienteNaoEncontradoException;
import com.orbitapay.clientes.domain.model.Cliente;
import com.orbitapay.clientes.domain.repository.ClienteRepository;

public class DesbloquearPeloCliente {

    private final ClienteRepository repositorio;
    private final PublicadorDeEventosDeCliente publicador;

    public DesbloquearPeloCliente(ClienteRepository repositorio, PublicadorDeEventosDeCliente publicador) {
        this.repositorio = repositorio;
        this.publicador = publicador;
    }

    public Cliente executar(String clienteId) {
        Cliente cliente = repositorio.buscarPorId(clienteId)
                .orElseThrow(() -> new ClienteNaoEncontradoException(clienteId));
        cliente.desbloquearPeloCliente();
        repositorio.salvar(cliente);
        publicador.publicar(new ClienteSituacaoAlterada(cliente.id(), false, null, Instant.now()));
        return cliente;
    }
}
