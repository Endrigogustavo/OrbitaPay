package com.orbitapay.contas.application.usecase;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;

import com.orbitapay.contas.domain.model.Dinheiro;

/** Reação ao evento {@code pagamento.confirmado}: credita o depósito na conta do cliente, uma única vez. */
public class CreditarDeposito {

    public record Comando(String pagamentoId, String clienteId, BigDecimal valor, String metodo) {
    }

    private final OperacaoComTrava operacao;
    private final Clock relogio;

    public CreditarDeposito(OperacaoComTrava operacao, Clock relogio) {
        this.operacao = operacao;
        this.relogio = relogio;
    }

    public void executar(Comando comando) {
        operacao.executar(comando.clienteId(), conta -> conta.creditarDeposito(comando.pagamentoId(),
                new Dinheiro(comando.valor()), comando.metodo(), Instant.now(relogio)));
    }
}
