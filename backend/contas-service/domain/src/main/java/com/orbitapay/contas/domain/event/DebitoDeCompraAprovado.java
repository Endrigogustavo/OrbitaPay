package com.orbitapay.contas.domain.event;

import java.time.Instant;

import com.orbitapay.contas.domain.model.Dinheiro;

public record DebitoDeCompraAprovado(String ordemId, String clienteId, Dinheiro valor, Dinheiro saldoRestante,
        Instant ocorridoEm) implements EventoDeConta {
}
