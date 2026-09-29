package com.orbitapay.ativos.domain.event;

import java.math.BigDecimal;

import com.orbitapay.ativos.domain.model.Ativo;
import com.orbitapay.ativos.domain.model.Bolsa;

public record DadosDoAtivo(String ticker, String nome, String setor, String bolsa, String moeda, BigDecimal cambio,
        BigDecimal cotacao, long quantidadeEmitida) {

    public static DadosDoAtivo de(Ativo ativo, Bolsa bolsa) {
        return new DadosDoAtivo(ativo.ticker().valor(), ativo.nome(), ativo.setor(), bolsa.codigo(), bolsa.moeda(),
                bolsa.cambio(), ativo.cotacao(), ativo.quantidadeEmitida());
    }
}
