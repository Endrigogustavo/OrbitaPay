package com.orbitapay.contas.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.orbitapay.contas.domain.model.Conta;

public record ContaResponse(
        String id,
        String clienteId,
        String titular,
        boolean titularBloqueado,
        String agencia,
        String numero,
        BigDecimal saldo,
        List<LancamentoResponse> lancamentos,
        Instant abertaEm) {

    public static ContaResponse de(Conta conta) {
        return new ContaResponse(conta.id(), conta.clienteId(), conta.nomeTitular(), conta.titularBloqueado(),
                conta.agencia(), conta.numero(), conta.saldo().valor(),
                conta.lancamentos().stream().map(LancamentoResponse::de).toList(), conta.abertaEm());
    }
}
