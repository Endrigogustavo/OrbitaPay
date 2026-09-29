package com.orbitapay.contas.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.orbitapay.contas.domain.model.Lancamento;

public record LancamentoResponse(String id, String tipo, BigDecimal valor, String descricao, String ordemId,
        Instant ocorridoEm) {

    public static LancamentoResponse de(Lancamento lancamento) {
        return new LancamentoResponse(lancamento.id(), lancamento.tipo().name(), lancamento.valor().valor(),
                lancamento.descricao(), lancamento.ordemId(), lancamento.ocorridoEm());
    }
}
