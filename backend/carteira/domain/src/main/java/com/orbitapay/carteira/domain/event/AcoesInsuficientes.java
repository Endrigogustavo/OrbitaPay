package com.orbitapay.carteira.domain.event;

import java.time.Instant;

public record AcoesInsuficientes(String ordemId, String clienteId, String ticker, String motivo, Instant ocorridoEm)
        implements EventoDeCarteira {
}
