package com.orbitapay.relatorios.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Algo que aconteceu em outro contexto e interessa aos relatórios, já traduzido para a linguagem deste contexto.
 * O {@code eventoId} da mensagem de origem é a identidade do fato: a mesma mensagem entregue duas vezes não é
 * contada em dobro.
 */
public record Fato(
        String eventoId,
        TipoDeFato tipo,
        String clienteId,
        String ticker,
        long quantidade,
        BigDecimal valor,
        String detalhe,
        Instant ocorridoEm) {

    public Fato {
        Objects.requireNonNull(eventoId);
        Objects.requireNonNull(tipo);
        Objects.requireNonNull(clienteId);
        Objects.requireNonNull(ocorridoEm);
        valor = valor == null ? BigDecimal.ZERO : valor;
    }
}
