package com.orbitapay.negociacao.application.usecase;

import java.math.BigDecimal;
import java.util.Map;

import com.orbitapay.negociacao.application.dto.AtivoDoCatalogo;
import com.orbitapay.negociacao.domain.model.AtivoNegociavel;
import com.orbitapay.negociacao.domain.repository.AtivoNegociavelRepository;

public class SincronizarAtivos {

    private final AtivoNegociavelRepository ativos;
    private final OperacaoComTrava operacao;

    public SincronizarAtivos(AtivoNegociavelRepository ativos, OperacaoComTrava operacao) {
        this.ativos = ativos;
        this.operacao = operacao;
    }

    public void registrar(AtivoDoCatalogo dados) {
        if (!ativos.existe(dados.ticker())) {
            ativos.inserir(AtivoNegociavel.listar(dados.ticker(), dados.nome(), dados.bolsa(), dados.moeda(),
                    dados.cambio(), dados.cotacao(), dados.quantidadeEmitida()));
            return;
        }
        operacao.executar(dados.ticker(), ativo -> {
            ativo.atualizarCadastro(dados.nome(), dados.bolsa(), dados.moeda(), dados.cambio(), dados.cotacao(),
                    dados.quantidadeEmitida());
            return ativo;
        });
    }

    public void retirar(String ticker) {
        if (ativos.existe(ticker)) {
            operacao.executar(ticker, ativo -> {
                ativo.retirarDeNegociacao();
                return ativo;
            });
        }
    }

    public void atualizarCotacoes(Map<String, BigDecimal> cotacoes) {
        ativos.atualizarCotacoes(cotacoes);
    }
}
