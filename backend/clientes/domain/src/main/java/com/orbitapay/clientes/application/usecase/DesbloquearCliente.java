package com.orbitapay.clientes.application.usecase;

import java.time.Clock;
import java.time.Instant;

import com.orbitapay.clientes.application.service.PublicadorDeEventosDeCliente;
import com.orbitapay.clientes.domain.event.ClienteSituacaoAlterada;
import com.orbitapay.clientes.domain.exception.ClienteNaoEncontradoException;
import com.orbitapay.clientes.domain.model.Cliente;
import com.orbitapay.clientes.domain.repository.ClienteRepository;

public class DesbloquearCliente {

    private final ClienteRepository repositorio;
    private final PublicadorDeEventosDeCliente publicador;
    private final Clock relogio;

    public DesbloquearCliente(ClienteRepository repositorio, PublicadorDeEventosDeCliente publicador, Clock relogio) {
        this.repositorio = repositorio;
        this.publicador = publicador;
        this.relogio = relogio;
    }

    /** O PIN já foi conferido pela Autenticação: o gateway só deixa a requisição passar com uma assinatura válida. */
    public Cliente peloCliente(String clienteId) {
        Cliente cliente = buscar(clienteId);
        cliente.desbloquearPeloCliente();
        return salvarEPublicar(cliente);
    }

    public Cliente peloGerente(String clienteId) {
        Cliente cliente = buscar(clienteId);
        cliente.desbloquearPeloGerente();
        return salvarEPublicar(cliente);
    }

    private Cliente buscar(String clienteId) {
        return repositorio.buscarPorId(clienteId).orElseThrow(() -> new ClienteNaoEncontradoException(clienteId));
    }

    private Cliente salvarEPublicar(Cliente cliente) {
        repositorio.salvar(cliente);
        publicador.publicar(new ClienteSituacaoAlterada(cliente.id(), false, null, Instant.now(relogio)));
        return cliente;
    }
}
