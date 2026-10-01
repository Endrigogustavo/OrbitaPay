package com.orbitapay.auth.messaging.mensagem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventoDeClienteMensagem(String eventoId, String evento, String clienteId, String email,
        boolean bloqueado) {
}
