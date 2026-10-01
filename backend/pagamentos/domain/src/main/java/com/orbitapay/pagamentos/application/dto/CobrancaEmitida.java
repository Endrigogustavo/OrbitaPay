package com.orbitapay.pagamentos.application.dto;

import com.orbitapay.pagamentos.domain.model.InstrucoesDePagamento;

public record CobrancaEmitida(String provedor, String referenciaExterna, InstrucoesDePagamento instrucoes) {
}
