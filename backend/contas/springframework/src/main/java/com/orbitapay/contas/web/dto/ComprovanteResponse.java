package com.orbitapay.contas.web.dto;

import java.math.BigDecimal;

import com.orbitapay.contas.application.dto.Comprovante;

public record ComprovanteResponse(LancamentoResponse lancamento, BigDecimal novoSaldo, String protocolo) {

    public static ComprovanteResponse de(Comprovante comprovante) {
        String id = comprovante.lancamento().id().replace("-", "");
        return new ComprovanteResponse(LancamentoResponse.de(comprovante.lancamento()),
                comprovante.conta().saldo().valor(), id.substring(0, 8).toUpperCase());
    }
}
