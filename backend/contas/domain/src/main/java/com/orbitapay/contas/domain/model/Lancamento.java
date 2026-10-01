package com.orbitapay.contas.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Linha do extrato. {@code referencia} aponta para o que originou o lançamento em outro contexto (a ordem da
 * Negociação ou o pagamento confirmado) e garante a idempotência quando a mesma mensagem chega duas vezes.
 */
public record Lancamento(String id, TipoLancamento tipo, Dinheiro valor, String descricao, String referencia,
        Instant ocorridoEm) {

    public Lancamento {
        Objects.requireNonNull(id);
        Objects.requireNonNull(tipo);
        Objects.requireNonNull(valor);
        Objects.requireNonNull(descricao);
        Objects.requireNonNull(ocorridoEm);
    }

    public boolean referenteA(String referencia) {
        return referencia != null && referencia.equals(this.referencia);
    }
}
