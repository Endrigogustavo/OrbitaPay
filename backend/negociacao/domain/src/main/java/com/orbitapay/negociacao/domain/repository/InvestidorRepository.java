package com.orbitapay.negociacao.domain.repository;

import java.util.Optional;

import com.orbitapay.negociacao.domain.model.Investidor;

public interface InvestidorRepository {

    Optional<Investidor> buscar(String clienteId);

    void salvar(Investidor investidor);

    void remover(String clienteId);
}
