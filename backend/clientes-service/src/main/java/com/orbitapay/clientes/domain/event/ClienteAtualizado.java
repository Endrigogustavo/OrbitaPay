package com.orbitapay.clientes.domain.event;

import java.time.Instant;

public record ClienteAtualizado(String clienteId, String nome, String email, Instant ocorridoEm)
        implements EventoDeCliente {
}
