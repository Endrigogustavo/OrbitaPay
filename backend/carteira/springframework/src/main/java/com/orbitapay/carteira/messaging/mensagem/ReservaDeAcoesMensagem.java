package com.orbitapay.carteira.messaging.mensagem;

import java.time.Instant;

public record ReservaDeAcoesMensagem(
        String eventoId,
        String evento,
        String ordemId,
        String clienteId,
        String ticker,
        Long quantidade,
        String motivo,
        String mensagem,
        Instant enviadoEm) {
}
