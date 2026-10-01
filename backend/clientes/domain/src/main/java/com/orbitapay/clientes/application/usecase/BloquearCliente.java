package com.orbitapay.clientes.application.usecase;

import java.time.Clock;
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
    private final Clock relogio;

    public BloquearCliente(ClienteRepository repositorio, PublicadorDeEventosDeCliente publicador, Clock relogio) {
        this.repositorio = repositorio;
        this.publicador = publicador;
        this.relogio = relogio;
    }

    public Cliente executar(String clienteId, MotivoBloqueio motivo) {
        Cliente cliente = repositorio.buscarPorId(clienteId)
                .orElseThrow(() -> new ClienteNaoEncontradoException(clienteId));
        cliente.bloquear(motivo);
        repositorio.salvar(cliente);
        publicador.publicar(new ClienteSituacaoAlterada(cliente.id(), true, motivo, Instant.now(relogio)));
        return cliente;
    }

    /**
     * Reação ao evento {@code credencial.bloqueada-por-pin} da Autenticação. É idempotente: um cliente já
     * bloqueado (ou já removido) não muda de situação.
     */
    public void porExcessoDeTentativasDePin(String clienteId) {
        repositorio.buscarPorId(clienteId)
                .filter(cliente -> !cliente.bloqueado())
                .ifPresent(cliente -> executar(cliente.id(), MotivoBloqueio.PIN));
    }
}
