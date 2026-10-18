package com.orbitapay.negociacao.adapter.controller.dto.response;

import java.math.BigDecimal;
import java.util.List;

import com.orbitapay.negociacao.entity.CarteiraValorizada;
import com.orbitapay.negociacao.entity.PosicaoValorizada;

public record CarteiraResponse(String clienteId, List<Posicao> posicoes, BigDecimal valorDeMercado, BigDecimal custo) {

    public record Posicao(String ticker, String nome, long quantidade, long quantidadeReservada, BigDecimal precoMedio,
            BigDecimal cotacao, String moeda, BigDecimal valorDeMercado, BigDecimal custo,
            BigDecimal resultadoPercentual) {
    }

    public static CarteiraResponse criarAPartirDe(CarteiraValorizada carteira) {
        List<Posicao> posicoes = carteira.posicoes().stream().map(CarteiraResponse::converterPosicao).toList();
        return new CarteiraResponse(carteira.clienteId(), posicoes, carteira.valorDeMercado(), carteira.custo());
    }

    private static Posicao converterPosicao(PosicaoValorizada posicao) {
        return new Posicao(posicao.ticker(), posicao.nome(), posicao.quantidade(), 0, posicao.precoMedio(),
                posicao.cotacao(), posicao.moeda(), posicao.valorDeMercado(), posicao.custo(),
                posicao.resultadoPercentual());
    }
}
