package com.orbitapay.clientes.adapter.out.messaging.mensagem;

import java.time.Instant;

public record ClienteRemovidoMensagem(
        String eventoId,
        String evento,
        String clienteId,
        String mensagem,
        Instant enviadoEm) {
}
