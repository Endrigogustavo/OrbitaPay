package com.orbitapay.clientes.application.usecase;

import java.util.Optional;

import com.orbitapay.clientes.domain.model.Cliente;
import com.orbitapay.clientes.domain.model.MotivoBloqueio;
import com.orbitapay.clientes.domain.repository.ClienteRepository;

public class BloquearPorExcessoDeTentativas {

    private final ClienteRepository repositorio;
    private final BloquearCliente bloquearCliente;

    public BloquearPorExcessoDeTentativas(ClienteRepository repositorio, BloquearCliente bloquearCliente) {
        this.repositorio = repositorio;
        this.bloquearCliente = bloquearCliente;
    }

    public void executar(String clienteId) {
        Optional<Cliente> cliente = repositorio.buscarPorId(clienteId);
        if (cliente.isPresent() && !cliente.get().bloqueado()) {
            bloquearCliente.executar(clienteId, MotivoBloqueio.PIN);
        }
    }
}
