package com.orbitapay.contas.application.usecase;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;

import com.orbitapay.contas.application.dto.Comprovante;
import com.orbitapay.contas.domain.model.Dinheiro;

public class Sacar {

    private final OperacaoComTrava operacao;
    private final Clock relogio;

    public Sacar(OperacaoComTrava operacao, Clock relogio) {
        this.operacao = operacao;
        this.relogio = relogio;
    }

    public Comprovante executar(String clienteId, BigDecimal valor) {
        return operacao.executar(clienteId,
                conta -> new Comprovante(conta, conta.sacar(new Dinheiro(valor), Instant.now(relogio))));
    }
}
