package com.orbitapay.negociacao.application.gateway;

import java.util.Optional;

import com.orbitapay.negociacao.entity.Carteira;

public interface ICarteiraGateway {

    void salvar(Carteira carteira);

    Optional<Carteira> buscarPorCliente(String clienteId);

    void removerPorCliente(String clienteId);
}
