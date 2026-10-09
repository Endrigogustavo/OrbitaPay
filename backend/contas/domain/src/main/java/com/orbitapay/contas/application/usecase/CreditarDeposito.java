package com.orbitapay.contas.application.usecase;

import java.math.BigDecimal;
import java.time.Instant;

import com.orbitapay.contas.domain.model.Dinheiro;
import com.orbitapay.contas.domain.repository.ContaTravada;
import com.orbitapay.contas.domain.repository.TravaDeConta;

public class CreditarDeposito {

    public record Comando(String pagamentoId, String clienteId, BigDecimal valor, String metodo) {
    }

    private final TravaDeConta trava;

    public CreditarDeposito(TravaDeConta trava) {
        this.trava = trava;
    }

    public void executar(Comando comando) {
        ContaTravada travada = trava.travar(comando.clienteId());
        try {
            travada.conta().creditarDeposito(comando.pagamentoId(), new Dinheiro(comando.valor()), comando.metodo(),
                    Instant.now());
            trava.salvarELiberar(travada);
        } catch (RuntimeException erro) {
            trava.liberar(travada);
            throw erro;
        }
    }
}
