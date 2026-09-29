package com.orbitapay.contas.domain.event;

import java.time.Instant;

public record DebitoDeCompraRecusado(String ordemId, String clienteId, String motivo, Instant ocorridoEm)
        implements EventoDeConta {
}
