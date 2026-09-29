package com.orbitapay.clientes.adapter.out.messaging.mensagem;

import java.time.Instant;

public record ClienteAtualizadoMensagem(
        String eventoId,
        String evento,
        String clienteId,
        String nome,
        String email,
        String mensagem,
        Instant enviadoEm) {
}
