package com.orbitapay.negociacao.domain.repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import com.orbitapay.negociacao.domain.model.AtivoNegociavel;

public interface AtivoNegociavelRepository {

    Optional<AtivoNegociavel> buscar(String ticker);

    boolean existe(String ticker);

    void inserir(AtivoNegociavel ativo);

    AtivoTravado travar(String ticker);

    void salvarELiberar(AtivoTravado travado);

    void liberar(AtivoTravado travado);

    void atualizarCotacoes(Map<String, BigDecimal> cotacoes);
}
