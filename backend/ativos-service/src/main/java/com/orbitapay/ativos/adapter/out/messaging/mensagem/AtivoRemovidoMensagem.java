package com.orbitapay.ativos.adapter.out.messaging.mensagem;

import java.time.Instant;

public record AtivoRemovidoMensagem(String eventoId, String evento, String ticker, String mensagem, Instant enviadoEm) {
}
