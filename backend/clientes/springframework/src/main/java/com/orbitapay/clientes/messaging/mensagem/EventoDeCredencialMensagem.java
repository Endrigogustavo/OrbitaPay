package com.orbitapay.clientes.messaging.mensagem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventoDeCredencialMensagem(String eventoId, String evento, String clienteId, int tentativas) {
}
