package com.orbitapay.contas.application.usecase;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;

import com.orbitapay.contas.application.dto.Comprovante;
import com.orbitapay.contas.application.port.in.SacarUseCase;
import com.orbitapay.contas.domain.model.Dinheiro;

public class Sacar implements SacarUseCase {

    private final OperacaoComTrava operacao;
    private final Clock relogio;

    public Sacar(OperacaoComTrava operacao, Clock relogio) {
        this.operacao = operacao;
        this.relogio = relogio;
    }

    @Override
    public Comprovante executar(String clienteId, BigDecimal valor) {
        return operacao.executar(clienteId,
                conta -> new Comprovante(conta, conta.sacar(new Dinheiro(valor), Instant.now(relogio))));
    }
}
