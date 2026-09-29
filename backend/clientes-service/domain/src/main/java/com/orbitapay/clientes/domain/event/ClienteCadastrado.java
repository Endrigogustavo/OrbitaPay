package com.orbitapay.clientes.domain.event;

import java.math.BigDecimal;
import java.time.Instant;

public record ClienteCadastrado(String clienteId, String nome, String email, BigDecimal depositoInicial,
        Instant ocorridoEm) implements EventoDeCliente {
}
