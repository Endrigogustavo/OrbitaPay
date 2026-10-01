package com.orbitapay.clientes.messaging.mensagem;

public record RegistroDeCredencialMensagem(String clienteId, String email, String pin) {

    @Override
    public String toString() {
        return "RegistroDeCredencialMensagem[clienteId=" + clienteId + ", email=" + email + ", pin=****]";
    }
}
