package com.orbitapay.clientes.domain.model;

import com.orbitapay.clientes.domain.exception.RegraDeNegocioException;

public record Cpf(String valor) {

    public Cpf {
        String digitos = valor == null ? "" : valor.replaceAll("\\D", "");
        if (digitos.length() != 11) {
            throw new RegraDeNegocioException("CPF precisa de 11 dígitos");
        }
        valor = digitos.substring(0, 3) + "." + digitos.substring(3, 6) + "."
                + digitos.substring(6, 9) + "-" + digitos.substring(9);
    }
}
