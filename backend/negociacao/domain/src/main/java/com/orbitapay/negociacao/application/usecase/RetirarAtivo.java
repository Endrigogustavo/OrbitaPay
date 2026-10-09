package com.orbitapay.negociacao.application.usecase;

import com.orbitapay.negociacao.domain.repository.AtivoNegociavelRepository;
import com.orbitapay.negociacao.domain.repository.AtivoTravado;
import com.orbitapay.negociacao.domain.repository.TravaDeAtivo;

public class RetirarAtivo {

    private final AtivoNegociavelRepository ativos;
    private final TravaDeAtivo trava;

    public RetirarAtivo(AtivoNegociavelRepository ativos, TravaDeAtivo trava) {
        this.ativos = ativos;
        this.trava = trava;
    }

    public void executar(String ticker) {
        if (!ativos.existe(ticker)) {
            return;
        }
        AtivoTravado travado = trava.travar(ticker);
        try {
            travado.ativo().retirarDeNegociacao();
            trava.salvarELiberar(travado);
        } catch (RuntimeException erro) {
            trava.liberar(travado);
            throw erro;
        }
    }
}
