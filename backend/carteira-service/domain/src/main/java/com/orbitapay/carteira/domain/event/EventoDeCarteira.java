package com.orbitapay.carteira.domain.event;

import java.time.Instant;

public sealed interface EventoDeCarteira permits AcoesReservadas, AcoesInsuficientes {

    String ordemId();

    String clienteId();

    Instant ocorridoEm();
}
