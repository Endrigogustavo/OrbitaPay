package com.orbitapay.pagamentos.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.orbitapay.pagamentos.domain.model.Pagamento;

public record PagamentoResponse(
        String id,
        BigDecimal valor,
        String metodo,
        String status,
        Instrucoes instrucoes,
        BigDecimal valorPago,
        Instant criadoEm,
        Instant concluidoEm) {

    public record Instrucoes(String codigo, String descricao, Instant validoAte) {
    }

    public static PagamentoResponse de(Pagamento pagamento) {
        return new PagamentoResponse(pagamento.id(), pagamento.valor().valor(), pagamento.metodo().name(),
                pagamento.status().name(), new Instrucoes(pagamento.instrucoes().codigo(),
                        pagamento.instrucoes().descricao(), pagamento.instrucoes().validoAte()),
                pagamento.valorPago() == null ? null : pagamento.valorPago().valor(), pagamento.criadoEm(),
                pagamento.concluidoEm());
    }
}
