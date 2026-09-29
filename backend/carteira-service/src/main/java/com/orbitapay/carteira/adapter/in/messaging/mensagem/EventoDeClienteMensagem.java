package com.orbitapay.carteira.adapter.in.messaging.mensagem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventoDeClienteMensagem(String eventoId, String evento, String clienteId) {
}
