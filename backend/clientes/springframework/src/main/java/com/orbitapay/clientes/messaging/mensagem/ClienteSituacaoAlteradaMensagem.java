package com.orbitapay.clientes.messaging.mensagem;

import java.time.Instant;

public record ClienteSituacaoAlteradaMensagem(
        String eventoId,
        String evento,
        String clienteId,
        boolean bloqueado,
        String motivo,
        String mensagem,
        Instant enviadoEm) {
}
