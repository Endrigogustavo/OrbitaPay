package com.orbitapay.auth.messaging.mensagem;

import java.time.Instant;

public record CredencialBloqueadaPorPinMensagem(
        String eventoId,
        String evento,
        String clienteId,
        int tentativas,
        String mensagem,
        Instant enviadoEm) {
}
