package com.orbitapay.clientes.domain.event;

import java.time.Instant;

import com.orbitapay.clientes.domain.model.MotivoBloqueio;

public record ClienteSituacaoAlterada(String clienteId, boolean bloqueado, MotivoBloqueio motivo, Instant ocorridoEm)
        implements EventoDeCliente {
}
