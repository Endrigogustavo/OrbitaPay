package com.orbitapay.negociacao.application.usecase;

import com.orbitapay.negociacao.application.dto.AtivoDoCatalogo;
import com.orbitapay.negociacao.domain.model.AtivoNegociavel;
import com.orbitapay.negociacao.domain.repository.AtivoNegociavelRepository;
import com.orbitapay.negociacao.domain.repository.AtivoTravado;
import com.orbitapay.negociacao.domain.repository.TravaDeAtivo;

public class RegistrarAtivo {

    private final AtivoNegociavelRepository ativos;
    private final TravaDeAtivo trava;

    public RegistrarAtivo(AtivoNegociavelRepository ativos, TravaDeAtivo trava) {
        this.ativos = ativos;
        this.trava = trava;
    }

    public void executar(AtivoDoCatalogo dados) {
        if (!ativos.existe(dados.ticker())) {
            ativos.inserir(AtivoNegociavel.listar(dados.ticker(), dados.nome(), dados.bolsa(), dados.moeda(),
                    dados.cambio(), dados.cotacao(), dados.quantidadeEmitida()));
            return;
        }
        AtivoTravado travado = trava.travar(dados.ticker());
        try {
            travado.ativo().atualizarCadastro(dados.nome(), dados.bolsa(), dados.moeda(), dados.cambio(),
                    dados.cotacao(), dados.quantidadeEmitida());
            trava.salvarELiberar(travado);
        } catch (RuntimeException erro) {
            trava.liberar(travado);
            throw erro;
        }
    }
}
