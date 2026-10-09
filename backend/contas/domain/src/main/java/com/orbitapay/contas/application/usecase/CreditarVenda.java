package com.orbitapay.contas.application.usecase;

import java.time.Instant;

import com.orbitapay.contas.application.dto.OrdemRecebida;
import com.orbitapay.contas.domain.model.Dinheiro;
import com.orbitapay.contas.domain.repository.ContaTravada;
import com.orbitapay.contas.domain.repository.TravaDeConta;

public class CreditarVenda {

    private final TravaDeConta trava;

    public CreditarVenda(TravaDeConta trava) {
        this.trava = trava;
    }

    public void executar(OrdemRecebida ordem) {
        String descricao = "Venda · " + ordem.quantidade() + " " + ordem.ticker();
        ContaTravada travada = trava.travar(ordem.clienteId());
        try {
            travada.conta().creditarVendaDeAcoes(ordem.ordemId(), new Dinheiro(ordem.valorTotal()), descricao,
                    Instant.now());
            trava.salvarELiberar(travada);
        } catch (RuntimeException erro) {
            trava.liberar(travada);
            throw erro;
        }
    }
}
