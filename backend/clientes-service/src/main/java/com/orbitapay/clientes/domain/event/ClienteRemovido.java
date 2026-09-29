package com.orbitapay.clientes.domain.event;

import java.time.Instant;

public record ClienteRemovido(String clienteId, Instant ocorridoEm) implements EventoDeCliente {
}
