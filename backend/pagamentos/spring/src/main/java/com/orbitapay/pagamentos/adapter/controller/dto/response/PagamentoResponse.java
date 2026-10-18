package com.orbitapay.pagamentos.adapter.controller.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

import com.orbitapay.pagamentos.entity.Pagamento;

public record PagamentoResponse(String id, BigDecimal valor, String metodo, String status, Instrucoes instrucoes,
        BigDecimal valorPago, Instant criadoEm, Instant concluidoEm) {

    public record Instrucoes(String codigo, String descricao, Instant validoAte) {
    }

    public static PagamentoResponse criarAPartirDe(Pagamento pagamento) {
        Instrucoes instrucoes = new Instrucoes(pagamento.instrucoes().codigo(), pagamento.instrucoes().descricao(),
                pagamento.instrucoes().validoAte());
        return new PagamentoResponse(pagamento.id(), pagamento.valor(), pagamento.metodo().name(),
                pagamento.status().name(), instrucoes, pagamento.valorPago(), pagamento.criadoEm(),
                pagamento.concluidoEm());
    }
}
