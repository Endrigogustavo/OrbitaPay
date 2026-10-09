package com.orbitapay.carteira.application.usecase;

import com.orbitapay.carteira.application.dto.OrdemRecebida;
import com.orbitapay.carteira.domain.repository.CarteiraTravada;
import com.orbitapay.carteira.domain.repository.TravaDeCarteira;

public class LiquidarVenda {

    private final TravaDeCarteira trava;

    public LiquidarVenda(TravaDeCarteira trava) {
        this.trava = trava;
    }

    public void executar(OrdemRecebida ordem) {
        CarteiraTravada travada = trava.travar(ordem.clienteId());
        try {
            travada.carteira().liquidarVenda(ordem.ordemId(), ordem.ticker());
            trava.salvarELiberar(travada);
        } catch (RuntimeException erro) {
            trava.liberar(travada);
            throw erro;
        }
    }
}
