package com.orbitapay.negociacao.domain.repository;

public interface TravaDeAtivo {

    AtivoTravado travar(String ticker);

    void salvarELiberar(AtivoTravado travado);

    void liberar(AtivoTravado travado);
}
