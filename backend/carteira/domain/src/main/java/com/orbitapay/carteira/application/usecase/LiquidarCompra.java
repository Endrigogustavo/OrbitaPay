package com.orbitapay.carteira.application.usecase;

import com.orbitapay.carteira.application.dto.OrdemRecebida;
import com.orbitapay.carteira.domain.repository.CarteiraTravada;
import com.orbitapay.carteira.domain.repository.TravaDeCarteira;

public class LiquidarCompra {

    private final TravaDeCarteira trava;

    public LiquidarCompra(TravaDeCarteira trava) {
        this.trava = trava;
    }

    public void executar(OrdemRecebida ordem) {
        CarteiraTravada travada = trava.travar(ordem.clienteId());
        try {
            travada.carteira().registrarCompra(ordem.ordemId(), ordem.ticker(), ordem.quantidade(), ordem.precoUnitario());
            trava.salvarELiberar(travada);
        } catch (RuntimeException erro) {
            trava.liberar(travada);
            throw erro;
        }
    }
}
