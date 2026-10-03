package com.orbitapay.contas.domain.model;

import java.time.Instant;

public record Lancamento(String id, TipoLancamento tipo, Dinheiro valor, String descricao, String referencia,
        Instant ocorridoEm) {

    public boolean referenteA(String referencia) {
        return referencia != null && referencia.equals(this.referencia);
    }
}
