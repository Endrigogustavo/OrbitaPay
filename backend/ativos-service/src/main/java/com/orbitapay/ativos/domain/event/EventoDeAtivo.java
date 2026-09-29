package com.orbitapay.ativos.domain.event;

import java.time.Instant;

public sealed interface EventoDeAtivo permits AtivoListado, AtivoAtualizado, AtivoRemovido, CotacoesAtualizadas {

    Instant ocorridoEm();
}
