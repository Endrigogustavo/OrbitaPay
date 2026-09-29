package com.orbitapay.contas.domain.exception;

import com.orbitapay.contas.domain.model.Dinheiro;

public class SaldoInsuficienteException extends RegraDeNegocioException {

    public SaldoInsuficienteException(Dinheiro disponivel) {
        super("Saldo insuficiente · disponível " + disponivel.formatado());
    }
}
