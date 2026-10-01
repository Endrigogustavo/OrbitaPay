package com.orbitapay.ativos.messaging.mensagem;

import java.time.Instant;

public record AtivoRemovidoMensagem(String eventoId, String evento, String ticker, String mensagem, Instant enviadoEm) {
}
