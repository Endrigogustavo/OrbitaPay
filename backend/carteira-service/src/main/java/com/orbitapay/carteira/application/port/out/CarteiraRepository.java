package com.orbitapay.carteira.application.port.out;

import java.util.Optional;

import com.orbitapay.carteira.domain.model.Carteira;

public interface CarteiraRepository {

    Optional<Carteira> buscarPorCliente(String clienteId);

    CarteiraTravada travarPorCliente(String clienteId);

    void salvarELiberar(CarteiraTravada travada);

    void liberar(CarteiraTravada travada);

    void removerPorCliente(String clienteId);
}
