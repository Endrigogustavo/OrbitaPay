package com.orbitapay.contas.domain.repository;

public interface TravaDeConta {

    ContaTravada travar(String clienteId);

    void salvarELiberar(ContaTravada travada);

    void liberar(ContaTravada travada);
}
