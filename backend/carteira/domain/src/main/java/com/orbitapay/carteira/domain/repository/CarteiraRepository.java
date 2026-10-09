package com.orbitapay.carteira.domain.repository;

import java.util.Optional;

import com.orbitapay.carteira.domain.model.Carteira;

public interface CarteiraRepository {

    Optional<Carteira> buscarPorCliente(String clienteId);

    void removerPorCliente(String clienteId);
}
