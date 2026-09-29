package com.orbitapay.clientes.application.usecase;

import java.time.Clock;
import java.time.Instant;

import com.orbitapay.clientes.application.port.in.RemoverClienteUseCase;
import com.orbitapay.clientes.application.port.out.ClienteRepository;
import com.orbitapay.clientes.application.port.out.PublicadorDeEventosDeCliente;
import com.orbitapay.clientes.domain.event.ClienteRemovido;
import com.orbitapay.clientes.domain.exception.ClienteNaoEncontradoException;

public class RemoverCliente implements RemoverClienteUseCase {

    private final ClienteRepository repositorio;
    private final PublicadorDeEventosDeCliente publicador;
    private final Clock relogio;

    public RemoverCliente(ClienteRepository repositorio, PublicadorDeEventosDeCliente publicador, Clock relogio) {
        this.repositorio = repositorio;
        this.publicador = publicador;
        this.relogio = relogio;
    }

    @Override
    public void executar(String clienteId) {
        if (repositorio.buscarPorId(clienteId).isEmpty()) {
            throw new ClienteNaoEncontradoException(clienteId);
        }
        repositorio.remover(clienteId);
        publicador.publicar(new ClienteRemovido(clienteId, Instant.now(relogio)));
    }
}
