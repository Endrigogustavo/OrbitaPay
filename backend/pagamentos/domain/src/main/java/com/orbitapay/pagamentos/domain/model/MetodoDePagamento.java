package com.orbitapay.pagamentos.domain.model;

import java.util.Locale;

import com.orbitapay.pagamentos.domain.exception.RegraDeNegocioException;

public enum MetodoDePagamento {
    PIX("Pix", Dinheiro.de("0.01")),
    BOLETO("Boleto", Dinheiro.de("10.00")),
    TED("TED", Dinheiro.de("1.00"));

    private final String rotulo;
    private final Dinheiro valorMinimo;

    MetodoDePagamento(String rotulo, Dinheiro valorMinimo) {
        this.rotulo = rotulo;
        this.valorMinimo = valorMinimo;
    }

    public static MetodoDePagamento de(String valor) {
        if (valor != null) {
            String normalizado = valor.trim().toUpperCase(Locale.ROOT);
            for (MetodoDePagamento metodo : values()) {
                if (metodo.name().equals(normalizado)) {
                    return metodo;
                }
            }
        }
        throw new RegraDeNegocioException("Método de pagamento inválido: " + valor);
    }

    public String rotulo() {
        return rotulo;
    }

    public Dinheiro valorMinimo() {
        return valorMinimo;
    }
}
