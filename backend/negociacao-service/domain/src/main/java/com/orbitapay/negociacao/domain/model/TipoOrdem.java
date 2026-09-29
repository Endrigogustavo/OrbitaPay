package com.orbitapay.negociacao.domain.model;

import com.orbitapay.negociacao.domain.exception.RegraDeNegocioException;

public enum TipoOrdem {
    COMPRA,
    VENDA;

    public static TipoOrdem de(String valor) {
        for (TipoOrdem tipo : values()) {
            if (tipo.name().equalsIgnoreCase(valor)) {
                return tipo;
            }
        }
        throw new RegraDeNegocioException("Tipo de ordem inválido: " + valor);
    }
}
