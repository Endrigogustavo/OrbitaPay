package com.orbitapay.clientes.messaging.mensagem;

import java.math.BigDecimal;
import java.time.Instant;

public record ClienteCadastradoMensagem(
        String eventoId,
        String evento,
        String clienteId,
        String nome,
        String email,
        BigDecimal depositoInicial,
        String mensagem,
        Instant enviadoEm) {
}
