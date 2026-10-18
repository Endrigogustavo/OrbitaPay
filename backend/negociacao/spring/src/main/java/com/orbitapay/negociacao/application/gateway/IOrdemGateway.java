package com.orbitapay.negociacao.application.gateway;

import java.util.List;
import java.util.Optional;

import com.orbitapay.negociacao.entity.Ordem;

public interface IOrdemGateway {

    void salvar(Ordem ordem);

    Optional<Ordem> buscarPorId(String ordemId);

    List<Ordem> listarDoCliente(String clienteId);
}
