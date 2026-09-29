package com.orbitapay.negociacao.domain.exception;

public class OfertaInsuficienteException extends RegraDeNegocioException {

    public OfertaInsuficienteException(String ticker, long disponivel) {
        super(disponivel == 0
                ? "Não há ações de " + ticker + " disponíveis no mercado"
                : "Apenas " + disponivel + " ações de " + ticker + " disponíveis no mercado");
    }
}
