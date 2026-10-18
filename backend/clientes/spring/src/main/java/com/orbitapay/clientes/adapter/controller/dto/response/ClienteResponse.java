package com.orbitapay.clientes.adapter.controller.dto.response;

import java.time.Instant;

import com.orbitapay.clientes.entity.Cliente;

public record ClienteResponse(String id, String nome, String email, String cpf, boolean bloqueado,
        String motivoBloqueio, Instant clienteDesde) {

    public static ClienteResponse criarAPartirDe(Cliente cliente) {
        return new ClienteResponse(cliente.id(), cliente.nome(), cliente.email(), cliente.cpf(), cliente.bloqueado(),
                cliente.motivoBloqueio() == null ? null : cliente.motivoBloqueio().name(), cliente.clienteDesde());
    }
}
