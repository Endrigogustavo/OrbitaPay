package com.orbitapay.ativos.domain.event;

import java.time.Instant;

public record AtivoRemovido(String ticker, Instant ocorridoEm) implements EventoDeAtivo {
}
