package com.orbitapay.clientes.adapter.controller.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

import com.orbitapay.clientes.entity.Lancamento;

public record LancamentoResponse(String id, String tipo, BigDecimal valor, String descricao, String referencia,
        Instant ocorridoEm) {

    public static LancamentoResponse criarAPartirDe(Lancamento lancamento) {
        return new LancamentoResponse(lancamento.id(), lancamento.tipo().name(), lancamento.valor(),
                lancamento.descricao(), lancamento.referencia(), lancamento.ocorridoEm());
    }
}
