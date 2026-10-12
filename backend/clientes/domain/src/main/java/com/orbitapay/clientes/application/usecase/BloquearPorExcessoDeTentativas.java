package com.orbitapay.clientes.application.usecase;

import java.time.Instant;
import java.util.Optional;

import com.orbitapay.clientes.application.service.PublicadorDeEventosDeCliente;
import com.orbitapay.clientes.domain.event.ClienteSituacaoAlterada;
import com.orbitapay.clientes.domain.model.Cliente;
import com.orbitapay.clientes.domain.model.MotivoBloqueio;
import com.orbitapay.clientes.domain.repository.ClienteRepository;

public class BloquearPorExcessoDeTentativas {

    private final ClienteRepository repositorio;
    private final PublicadorDeEventosDeCliente publicador;

    public BloquearPorExcessoDeTentativas(ClienteRepository repositorio, PublicadorDeEventosDeCliente publicador) {
        this.repositorio = repositorio;
        this.publicador = publicador;
    }

    public void executar(String clienteId) {
        Optional<Cliente> encontrado = repositorio.buscarPorId(clienteId);
        if (encontrado.isEmpty() || encontrado.get().bloqueado()) {
            return;
        }
        Cliente cliente = encontrado.get();
        cliente.bloquear(MotivoBloqueio.PIN);
        repositorio.salvar(cliente);
        publicador.publicar(new ClienteSituacaoAlterada(cliente.id(), true, MotivoBloqueio.PIN, Instant.now()));
    }
}
