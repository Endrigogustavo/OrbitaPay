package com.orbitapay.ativos.domain.event;

import java.time.Instant;

public record AtivoAtualizado(DadosDoAtivo ativo, Instant ocorridoEm) implements EventoDeAtivo {
}
