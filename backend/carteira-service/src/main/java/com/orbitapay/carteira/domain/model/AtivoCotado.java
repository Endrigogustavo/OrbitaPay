package com.orbitapay.carteira.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record AtivoCotado(String ticker, String nome, String moeda, BigDecimal cambio, BigDecimal cotacao) {

    public AtivoCotado {
        Objects.requireNonNull(ticker);
        Objects.requireNonNull(cambio);
        Objects.requireNonNull(cotacao);
    }

    public AtivoCotado comCotacao(BigDecimal novaCotacao) {
        return new AtivoCotado(ticker, nome, moeda, cambio, novaCotacao);
    }

    public BigDecimal emReais(BigDecimal valorNaMoedaDoAtivo) {
        return valorNaMoedaDoAtivo.multiply(cambio);
    }
}
