package com.orbitapay.clientes.application.port.in;

import com.orbitapay.clientes.domain.model.Cliente;

public interface AtualizarDadosDoClienteUseCase {

    Cliente executar(Comando comando);

    record Comando(String clienteId, String nome, String email, String cpf) {
    }
}
