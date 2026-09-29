package com.orbitapay.negociacao.domain.event;

import java.time.Instant;

import com.orbitapay.negociacao.domain.model.Ordem;

public sealed interface EventoDeOrdem
        permits OrdemDeCompraSolicitada, OrdemDeVendaSolicitada, OrdemExecutada, OrdemRejeitada {

    Ordem ordem();

    Instant ocorridoEm();
}
