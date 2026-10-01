package com.orbitapay.clientes.messaging.mensagem;

import java.time.Instant;

public record ClienteRemovidoMensagem(
        String eventoId,
        String evento,
        String clienteId,
        String mensagem,
        Instant enviadoEm) {
}
