package com.orbitapay.contas.adapter.out.messaging.mensagem;

import java.time.Instant;

public record DebitoDeCompraRecusadoMensagem(
        String eventoId,
        String evento,
        String ordemId,
        String clienteId,
        String motivo,
        String mensagem,
        Instant enviadoEm) {
}
