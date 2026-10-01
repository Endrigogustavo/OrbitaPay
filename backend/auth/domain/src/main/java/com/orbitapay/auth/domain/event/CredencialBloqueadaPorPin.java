package com.orbitapay.auth.domain.event;

import java.time.Instant;

public record CredencialBloqueadaPorPin(String clienteId, int tentativas, Instant ocorridoEm)
        implements EventoDeCredencial {
}
