package com.orbitapay.ativos.application.port.in;

import java.math.BigDecimal;

import com.orbitapay.ativos.application.dto.AtivoCotado;

public interface ListarAtivoNaBolsaUseCase {

    AtivoCotado executar(Comando comando);

    record Comando(String ticker, String nome, String setor, String bolsa, BigDecimal cotacao,
            Long quantidadeEmitida) {
    }
}
