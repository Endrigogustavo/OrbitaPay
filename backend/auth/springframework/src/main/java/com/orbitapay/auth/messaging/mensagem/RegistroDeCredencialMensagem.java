package com.orbitapay.auth.messaging.mensagem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RegistroDeCredencialMensagem(String clienteId, String email, String pin) {

    @Override
    public String toString() {
        return "RegistroDeCredencialMensagem[clienteId=" + clienteId + ", email=" + email + ", pin=****]";
    }
}
