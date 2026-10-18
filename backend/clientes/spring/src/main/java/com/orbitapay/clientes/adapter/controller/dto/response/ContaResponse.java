package com.orbitapay.clientes.adapter.controller.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.orbitapay.clientes.entity.Conta;

public record ContaResponse(String id, String clienteId, String agencia, String numero, BigDecimal saldo,
        List<LancamentoResponse> lancamentos, Instant abertaEm) {

    public static ContaResponse criarAPartirDe(Conta conta) {
        return new ContaResponse(conta.id(), conta.clienteId(), conta.agencia(), conta.numero(), conta.saldo(),
                conta.lancamentos().stream().map(LancamentoResponse::criarAPartirDe).toList(), conta.abertaEm());
    }
}
