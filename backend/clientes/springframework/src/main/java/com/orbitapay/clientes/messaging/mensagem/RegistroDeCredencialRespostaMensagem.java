package com.orbitapay.clientes.messaging.mensagem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RegistroDeCredencialRespostaMensagem(boolean registrada, String motivo) {
}
