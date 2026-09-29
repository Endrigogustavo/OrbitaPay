package com.orbitapay.clientes.domain.model;

import com.orbitapay.clientes.domain.exception.RegraDeNegocioException;

public record Pin(String valor) {

    public Pin {
        if (valor == null || !valor.matches("\\d{4}")) {
            throw new RegraDeNegocioException("O PIN tem 4 dígitos");
        }
    }

    @Override
    public String toString() {
        return "****";
    }
}
