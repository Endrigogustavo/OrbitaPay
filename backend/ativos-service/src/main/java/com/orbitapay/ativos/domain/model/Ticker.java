package com.orbitapay.ativos.domain.model;

import java.util.Locale;

import com.orbitapay.ativos.domain.exception.RegraDeNegocioException;

public record Ticker(String valor) {

    public Ticker {
        String normalizado = valor == null ? "" : valor.trim().toUpperCase(Locale.ROOT);
        if (!normalizado.matches("[A-Z0-9]{2,8}")) {
            throw new RegraDeNegocioException("Ticker com 2 a 8 letras ou números");
        }
        valor = normalizado;
    }
}
