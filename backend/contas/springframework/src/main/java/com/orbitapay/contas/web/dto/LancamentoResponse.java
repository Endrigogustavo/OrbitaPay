package com.orbitapay.contas.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.orbitapay.contas.domain.model.Lancamento;

public record LancamentoResponse(String id, String tipo, BigDecimal valor, String descricao, String referencia,
        Instant ocorridoEm) {

    public static LancamentoResponse de(Lancamento lancamento) {
        return new LancamentoResponse(lancamento.id(), lancamento.tipo().name(), lancamento.valor().valor(),
                lancamento.descricao(), lancamento.referencia(), lancamento.ocorridoEm());
    }
}
