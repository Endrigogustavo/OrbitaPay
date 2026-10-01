package com.orbitapay.carteira.messaging.mensagem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventoDeClienteMensagem(String eventoId, String evento, String clienteId) {
}
