package com.orbitapay.negociacao.application.usecase;

import java.math.BigDecimal;
import java.util.Map;

import com.orbitapay.negociacao.application.dto.AtivoDoCatalogo;
import com.orbitapay.negociacao.application.port.in.SincronizarAtivosUseCase;
import com.orbitapay.negociacao.application.port.out.AtivoNegociavelRepository;
import com.orbitapay.negociacao.domain.model.AtivoNegociavel;

public class SincronizarAtivos implements SincronizarAtivosUseCase {

    private final AtivoNegociavelRepository ativos;
    private final OperacaoComTrava operacao;

    public SincronizarAtivos(AtivoNegociavelRepository ativos, OperacaoComTrava operacao) {
        this.ativos = ativos;
        this.operacao = operacao;
    }

    @Override
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

    @Override
    public void retirar(String ticker) {
        if (ativos.existe(ticker)) {
            operacao.executar(ticker, ativo -> {
                ativo.retirarDeNegociacao();
                return ativo;
            });
        }
    }

    @Override
    public void atualizarCotacoes(Map<String, BigDecimal> cotacoes) {
        ativos.atualizarCotacoes(cotacoes);
    }
}
