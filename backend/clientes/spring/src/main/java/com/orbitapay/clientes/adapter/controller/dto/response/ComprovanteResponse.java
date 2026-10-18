package com.orbitapay.clientes.adapter.controller.dto.response;

import java.math.BigDecimal;

import com.orbitapay.clientes.entity.Comprovante;

public record ComprovanteResponse(LancamentoResponse lancamento, BigDecimal novoSaldo, String protocolo) {

    public static ComprovanteResponse criarAPartirDe(Comprovante comprovante) {
        return new ComprovanteResponse(LancamentoResponse.criarAPartirDe(comprovante.lancamento()),
                comprovante.novoSaldo(), comprovante.protocolo());
    }
}
