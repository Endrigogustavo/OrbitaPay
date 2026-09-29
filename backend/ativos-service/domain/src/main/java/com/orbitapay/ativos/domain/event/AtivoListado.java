package com.orbitapay.ativos.domain.event;

import java.time.Instant;

public record AtivoListado(DadosDoAtivo ativo, Instant ocorridoEm) implements EventoDeAtivo {
}
