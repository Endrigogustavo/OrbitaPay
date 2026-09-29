package com.orbitapay.clientes.domain.model;

import com.orbitapay.clientes.domain.exception.RegraDeNegocioException;

public record NomeCompleto(String valor) {

    public NomeCompleto {
        String limpo = valor == null ? "" : valor.trim().replaceAll("\\s+", " ");
        if (limpo.split(" ").length < 2) {
            throw new RegraDeNegocioException("Informe nome e sobrenome");
        }
        valor = limpo;
    }
}
