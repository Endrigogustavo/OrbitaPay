package com.orbitapay.negociacao.domain.repository;

import com.orbitapay.negociacao.domain.model.Ordem;

public interface TravaDeAtivo {

    AtivoTravado travar(String ticker);

    void salvarELiberar(AtivoTravado travado);

    void salvarComOrdemELiberar(AtivoTravado travado, Ordem ordem);

    void liberar(AtivoTravado travado);
}
