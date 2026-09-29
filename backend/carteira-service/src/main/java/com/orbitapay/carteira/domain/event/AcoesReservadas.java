package com.orbitapay.carteira.domain.event;

import java.time.Instant;

public record AcoesReservadas(String ordemId, String clienteId, String ticker, long quantidade, Instant ocorridoEm)
        implements EventoDeCarteira {
}
