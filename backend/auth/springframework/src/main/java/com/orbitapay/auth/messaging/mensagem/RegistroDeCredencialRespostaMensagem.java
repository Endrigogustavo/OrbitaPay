package com.orbitapay.auth.messaging.mensagem;

public record RegistroDeCredencialRespostaMensagem(boolean registrada, String motivo) {

    public static RegistroDeCredencialRespostaMensagem sucesso() {
        return new RegistroDeCredencialRespostaMensagem(true, null);
    }

    public static RegistroDeCredencialRespostaMensagem recusada(String motivo) {
        return new RegistroDeCredencialRespostaMensagem(false, motivo);
    }
}
