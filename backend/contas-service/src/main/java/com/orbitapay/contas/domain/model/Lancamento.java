package com.orbitapay.contas.domain.model;

import java.time.Instant;
import java.util.Objects;

public record Lancamento(String id, TipoLancamento tipo, Dinheiro valor, String descricao, String ordemId,
        Instant ocorridoEm) {

    public Lancamento {
        Objects.requireNonNull(id);
        Objects.requireNonNull(tipo);
        Objects.requireNonNull(valor);
        Objects.requireNonNull(descricao);
        Objects.requireNonNull(ocorridoEm);
    }

    public boolean referenteA(String ordemId) {
        return ordemId != null && ordemId.equals(this.ordemId);
    }
}
