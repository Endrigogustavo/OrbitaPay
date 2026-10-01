package com.orbitapay.auth.domain.model;

import java.util.Locale;
import java.util.regex.Pattern;

import com.orbitapay.auth.domain.exception.RegraDeNegocioException;

public record Email(String valor) {

    private static final Pattern FORMATO = Pattern.compile("^\\S+@\\S+\\.\\S+$");

    public Email {
        if (valor == null || !FORMATO.matcher(valor.trim()).matches()) {
            throw new RegraDeNegocioException("E-mail inválido");
        }
        valor = valor.trim().toLowerCase(Locale.ROOT);
    }
}
