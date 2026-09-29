package com.orbitapay.contas.domain.event;

import java.time.Instant;

public sealed interface EventoDeConta permits DebitoDeCompraAprovado, DebitoDeCompraRecusado {

    String ordemId();

    String clienteId();

    Instant ocorridoEm();
}
