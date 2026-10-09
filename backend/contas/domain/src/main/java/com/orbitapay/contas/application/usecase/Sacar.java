package com.orbitapay.contas.application.usecase;

import java.math.BigDecimal;
import java.time.Instant;

import com.orbitapay.contas.application.dto.Comprovante;
import com.orbitapay.contas.domain.model.Conta;
import com.orbitapay.contas.domain.model.Dinheiro;
import com.orbitapay.contas.domain.model.Lancamento;
import com.orbitapay.contas.domain.repository.ContaTravada;
import com.orbitapay.contas.domain.repository.TravaDeConta;

public class Sacar {

    private final TravaDeConta trava;

    public Sacar(TravaDeConta trava) {
        this.trava = trava;
    }

    public Comprovante executar(String clienteId, BigDecimal valor) {
        ContaTravada travada = trava.travar(clienteId);
        try {
            Conta conta = travada.conta();
            Lancamento saque = conta.sacar(new Dinheiro(valor), Instant.now());
            trava.salvarELiberar(travada);
            return new Comprovante(conta, saque);
        } catch (RuntimeException erro) {
            trava.liberar(travada);
            throw erro;
        }
    }
}
