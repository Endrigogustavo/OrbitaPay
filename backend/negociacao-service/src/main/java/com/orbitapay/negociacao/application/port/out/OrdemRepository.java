package com.orbitapay.negociacao.application.port.out;

import java.util.List;
import java.util.Optional;

import com.orbitapay.negociacao.domain.model.Ordem;

public interface OrdemRepository {

    String proximoId();

    void salvar(Ordem ordem);

    Optional<Ordem> buscar(String ordemId);

    List<Ordem> listarDoCliente(String clienteId);
}
