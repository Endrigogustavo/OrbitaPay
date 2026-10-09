package com.orbitapay.clientes.application.usecase;

import java.time.Instant;

import com.orbitapay.clientes.application.service.PublicadorDeEventosDeCliente;
import com.orbitapay.clientes.domain.event.ClienteSituacaoAlterada;
import com.orbitapay.clientes.domain.exception.ClienteNaoEncontradoException;
import com.orbitapay.clientes.domain.model.Cliente;
import com.orbitapay.clientes.domain.repository.ClienteRepository;

public class DesbloquearPeloGerente {

    private final ClienteRepository repositorio;
    private final PublicadorDeEventosDeCliente publicador;

    public DesbloquearPeloGerente(ClienteRepository repositorio, PublicadorDeEventosDeCliente publicador) {
        this.repositorio = repositorio;
        this.publicador = publicador;
    }

    public Cliente executar(String clienteId) {
        Cliente cliente = repositorio.buscarPorId(clienteId)
                .orElseThrow(() -> new ClienteNaoEncontradoException(clienteId));
        cliente.desbloquearPeloGerente();
        repositorio.salvar(cliente);
        publicador.publicar(new ClienteSituacaoAlterada(cliente.id(), false, null, Instant.now()));
        return cliente;
    }
}
