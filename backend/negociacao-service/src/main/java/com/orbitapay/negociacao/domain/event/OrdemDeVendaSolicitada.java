package com.orbitapay.negociacao.domain.event;

import java.time.Instant;

import com.orbitapay.negociacao.domain.model.Ordem;

public record OrdemDeVendaSolicitada(Ordem ordem, Instant ocorridoEm) implements EventoDeOrdem {
}
