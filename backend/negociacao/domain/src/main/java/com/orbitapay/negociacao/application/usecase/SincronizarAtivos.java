package com.orbitapay.negociacao.application.usecase;

import java.math.BigDecimal;
import java.util.Map;

import com.orbitapay.negociacao.application.dto.AtivoDoCatalogo;
import com.orbitapay.negociacao.domain.model.AtivoNegociavel;
import com.orbitapay.negociacao.domain.repository.AtivoNegociavelRepository;
import com.orbitapay.negociacao.domain.repository.AtivoTravado;

public class SincronizarAtivos {

    private final AtivoNegociavelRepository ativos;

    public SincronizarAtivos(AtivoNegociavelRepository ativos) {
        this.ativos = ativos;
    }

    public void registrar(AtivoDoCatalogo dados) {
        if (!ativos.existe(dados.ticker())) {
            ativos.inserir(AtivoNegociavel.listar(dados.ticker(), dados.nome(), dados.bolsa(), dados.moeda(),
                    dados.cambio(), dados.cotacao(), dados.quantidadeEmitida()));
            return;
        }
        AtivoTravado travado = ativos.travar(dados.ticker());
        try {
            travado.ativo().atualizarCadastro(dados.nome(), dados.bolsa(), dados.moeda(), dados.cambio(),
                    dados.cotacao(), dados.quantidadeEmitida());
            ativos.salvarELiberar(travado);
        } catch (RuntimeException erro) {
            ativos.liberar(travado);
            throw erro;
        }
    }

    public void retirar(String ticker) {
        if (!ativos.existe(ticker)) {
            return;
        }
        AtivoTravado travado = ativos.travar(ticker);
        try {
            travado.ativo().retirarDeNegociacao();
            ativos.salvarELiberar(travado);
        } catch (RuntimeException erro) {
            ativos.liberar(travado);
            throw erro;
        }
    }

    public void atualizarCotacoes(Map<String, BigDecimal> cotacoes) {
        ativos.atualizarCotacoes(cotacoes);
    }
}
