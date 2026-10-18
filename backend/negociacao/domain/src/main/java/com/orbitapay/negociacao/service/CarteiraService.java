package com.orbitapay.negociacao.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.orbitapay.negociacao.entity.Carteira;
import com.orbitapay.negociacao.entity.CarteiraValorizada;
import com.orbitapay.negociacao.entity.CotacaoDoAtivo;
import com.orbitapay.negociacao.entity.Posicao;
import com.orbitapay.negociacao.entity.PosicaoValorizada;
import com.orbitapay.negociacao.exception.RegraDeNegocioException;

public class CarteiraService {

    public Carteira adicionarAcoesCompradas(Carteira carteira, String ticker, long quantidade, BigDecimal precoUnitario) {
        Posicao posicaoAtual = buscarPosicao(carteira, ticker);
        long quantidadeFinal = posicaoAtual.quantidade() + quantidade;
        BigDecimal custoAnterior = posicaoAtual.precoMedio().multiply(BigDecimal.valueOf(posicaoAtual.quantidade()));
        BigDecimal custoDaCompra = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
        BigDecimal novoPrecoMedio = custoAnterior.add(custoDaCompra)
                .divide(BigDecimal.valueOf(quantidadeFinal), 4, RoundingMode.HALF_EVEN);
        return substituirPosicao(carteira, new Posicao(ticker, quantidadeFinal, novoPrecoMedio));
    }

    public Carteira retirarAcoesVendidas(Carteira carteira, String ticker, long quantidade) {
        Posicao posicaoAtual = buscarPosicao(carteira, ticker);
        if (posicaoAtual.quantidade() <= 0) {
            throw new RegraDeNegocioException("Você não possui " + ticker);
        }
        if (posicaoAtual.quantidade() < quantidade) {
            throw new RegraDeNegocioException("Você tem apenas " + posicaoAtual.quantidade() + " " + ticker
                    + " disponíveis para venda");
        }
        return substituirPosicao(carteira,
                new Posicao(ticker, posicaoAtual.quantidade() - quantidade, posicaoAtual.precoMedio()));
    }

    public Carteira devolverAcoes(Carteira carteira, String ticker, long quantidade) {
        Posicao posicaoAtual = buscarPosicao(carteira, ticker);
        return substituirPosicao(carteira,
                new Posicao(ticker, posicaoAtual.quantidade() + quantidade, posicaoAtual.precoMedio()));
    }

    public CarteiraValorizada valorizarCarteira(Carteira carteira, Map<String, CotacaoDoAtivo> cotacoesPorTicker) {
        List<PosicaoValorizada> posicoesValorizadas = new ArrayList<>();
        BigDecimal valorDeMercadoTotal = BigDecimal.ZERO;
        BigDecimal custoTotal = BigDecimal.ZERO;
        for (Posicao posicao : carteira.posicoes()) {
            if (posicao.quantidade() <= 0) {
                continue;
            }
            PosicaoValorizada posicaoValorizada = valorizarPosicao(posicao, cotacoesPorTicker.get(posicao.ticker()));
            posicoesValorizadas.add(posicaoValorizada);
            valorDeMercadoTotal = valorDeMercadoTotal.add(posicaoValorizada.valorDeMercado());
            custoTotal = custoTotal.add(posicaoValorizada.custo());
        }
        return new CarteiraValorizada(carteira.clienteId(), posicoesValorizadas, valorDeMercadoTotal, custoTotal);
    }

    private static Posicao buscarPosicao(Carteira carteira, String ticker) {
        for (Posicao posicao : carteira.posicoes()) {
            if (posicao.ticker().equals(ticker)) {
                return posicao;
            }
        }
        return new Posicao(ticker, 0, BigDecimal.ZERO);
    }

    private static Carteira substituirPosicao(Carteira carteira, Posicao novaPosicao) {
        List<Posicao> posicoesAtualizadas = new ArrayList<>();
        for (Posicao posicao : carteira.posicoes()) {
            if (!posicao.ticker().equals(novaPosicao.ticker())) {
                posicoesAtualizadas.add(posicao);
            }
        }
        posicoesAtualizadas.add(novaPosicao);
        return new Carteira(carteira.clienteId(), posicoesAtualizadas);
    }

    private static PosicaoValorizada valorizarPosicao(Posicao posicao, CotacaoDoAtivo cotacao) {
        BigDecimal quantidade = BigDecimal.valueOf(posicao.quantidade());
        BigDecimal cotacaoAtual = cotacao == null ? posicao.precoMedio() : cotacao.cotacao();
        BigDecimal cambio = cotacao == null ? BigDecimal.ONE : cotacao.cambio();
        BigDecimal valorDeMercado = cotacaoAtual.multiply(quantidade).multiply(cambio).setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal custo = posicao.precoMedio().multiply(quantidade).multiply(cambio).setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal resultadoPercentual = custo.signum() == 0 ? BigDecimal.ZERO
                : valorDeMercado.divide(custo, 6, RoundingMode.HALF_EVEN).subtract(BigDecimal.ONE)
                        .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_EVEN);
        return new PosicaoValorizada(posicao.ticker(), cotacao == null ? posicao.ticker() : cotacao.nome(),
                posicao.quantidade(), posicao.precoMedio(), cotacaoAtual, cotacao == null ? null : cotacao.moeda(),
                valorDeMercado, custo, resultadoPercentual);
    }
}
