package com.orbitapay.carteira.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record CarteiraValorizada(String clienteId, List<PosicaoValorizada> posicoes, BigDecimal valorDeMercado,
        BigDecimal custo) {

    public record PosicaoValorizada(String ticker, String nome, long quantidade, long quantidadeReservada,
            BigDecimal precoMedio, BigDecimal cotacao, String moeda, BigDecimal valorDeMercado, BigDecimal custo,
            BigDecimal resultadoPercentual) {
    }
}
