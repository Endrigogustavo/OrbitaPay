package com.orbitapay.clientes.messaging.mensagem;

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
