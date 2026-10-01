package com.orbitapay.negociacao.application.usecase;

import java.util.function.Function;

import com.orbitapay.negociacao.domain.model.AtivoNegociavel;
import com.orbitapay.negociacao.domain.repository.AtivoNegociavelRepository;
import com.orbitapay.negociacao.domain.repository.AtivoTravado;

public class OperacaoComTrava {

    private final AtivoNegociavelRepository repositorio;

    public OperacaoComTrava(AtivoNegociavelRepository repositorio) {
        this.repositorio = repositorio;
    }

    public <T> T executar(String ticker, Function<AtivoNegociavel, T> operacao) {
        AtivoTravado travado = repositorio.travar(ticker);
        try {
            T resultado = operacao.apply(travado.ativo());
            repositorio.salvarELiberar(travado);
            return resultado;
        } catch (RuntimeException e) {
            repositorio.liberar(travado);
            throw e;
        }
    }
}
