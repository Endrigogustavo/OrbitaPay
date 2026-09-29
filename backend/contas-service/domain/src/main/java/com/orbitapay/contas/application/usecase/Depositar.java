package com.orbitapay.contas.application.usecase;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Set;

import com.orbitapay.contas.application.dto.Comprovante;
import com.orbitapay.contas.application.port.in.DepositarUseCase;
import com.orbitapay.contas.domain.exception.RegraDeNegocioException;
import com.orbitapay.contas.domain.model.Dinheiro;

public class Depositar implements DepositarUseCase {

    private static final Set<String> METODOS = Set.of("PIX", "TED", "Boleto");

    private final OperacaoComTrava operacao;
    private final Clock relogio;

    public Depositar(OperacaoComTrava operacao, Clock relogio) {
        this.operacao = operacao;
        this.relogio = relogio;
    }

    @Override
    public Comprovante executar(String clienteId, BigDecimal valor, String metodo) {
        if (!METODOS.contains(metodo)) {
            throw new RegraDeNegocioException("Método de depósito inválido");
        }
        return operacao.executar(clienteId,
                conta -> new Comprovante(conta, conta.depositar(new Dinheiro(valor), metodo, Instant.now(relogio))));
    }
}
