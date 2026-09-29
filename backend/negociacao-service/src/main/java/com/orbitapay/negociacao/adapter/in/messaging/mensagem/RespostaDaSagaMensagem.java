package com.orbitapay.negociacao.adapter.in.messaging.mensagem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RespostaDaSagaMensagem(String eventoId, String evento, String ordemId, String clienteId, String motivo) {
}
