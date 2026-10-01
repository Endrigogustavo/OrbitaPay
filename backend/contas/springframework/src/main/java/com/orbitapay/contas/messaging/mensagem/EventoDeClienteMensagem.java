package com.orbitapay.contas.messaging.mensagem;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventoDeClienteMensagem(
        String eventoId,
        String evento,
        String clienteId,
        String nome,
        BigDecimal depositoInicial,
        boolean bloqueado) {
}
