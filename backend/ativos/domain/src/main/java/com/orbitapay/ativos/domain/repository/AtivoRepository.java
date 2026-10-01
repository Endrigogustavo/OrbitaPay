package com.orbitapay.ativos.domain.repository;

import java.util.List;
import java.util.Optional;

import com.orbitapay.ativos.domain.model.Ativo;
import com.orbitapay.ativos.domain.model.Ticker;

public interface AtivoRepository {

    Optional<Ativo> buscar(Ticker ticker);

    boolean existe(Ticker ticker);

    List<Ativo> listar();

    void salvar(Ativo ativo);

    void atualizarCotacoes(List<Ativo> ativos);

    void remover(Ticker ticker);

    boolean vazio();
}
