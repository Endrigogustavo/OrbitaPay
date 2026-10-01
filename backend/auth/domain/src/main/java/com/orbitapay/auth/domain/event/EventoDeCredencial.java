package com.orbitapay.auth.domain.event;

import java.time.Instant;

public sealed interface EventoDeCredencial permits CredencialBloqueadaPorPin {

    String clienteId();

    Instant ocorridoEm();
}
