package com.orbitapay.negociacao.messaging.mensagem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventoDeClienteMensagem(String eventoId, String evento, String clienteId, boolean bloqueado) {
}
