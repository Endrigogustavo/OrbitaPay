package com.orbitapay.carteira.domain.repository;

public interface TravaDeCarteira {

    CarteiraTravada travar(String clienteId);

    void salvarELiberar(CarteiraTravada travada);

    void liberar(CarteiraTravada travada);
}
