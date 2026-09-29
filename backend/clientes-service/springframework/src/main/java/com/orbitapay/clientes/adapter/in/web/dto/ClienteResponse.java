package com.orbitapay.clientes.adapter.in.web.dto;

import java.time.Instant;

import com.orbitapay.clientes.domain.model.Cliente;

public record ClienteResponse(
        String id,
        String nome,
        String email,
        String cpf,
        boolean bloqueado,
        String motivoBloqueio,
        int tentativasFalhas,
        int tentativasPermitidas,
        Instant clienteDesde) {

    public static ClienteResponse de(Cliente cliente) {
        return new ClienteResponse(
                cliente.id(),
                cliente.nome().valor(),
                cliente.email().valor(),
                cliente.cpf().valor(),
                cliente.bloqueado(),
                cliente.motivoBloqueio() == null ? null : cliente.motivoBloqueio().name(),
                cliente.tentativasFalhas(),
                Cliente.LIMITE_TENTATIVAS_PIN,
                cliente.clienteDesde());
    }
}
