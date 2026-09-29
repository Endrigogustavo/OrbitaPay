package com.orbitapay.clientes.application.port.in;

import com.orbitapay.clientes.domain.model.Cliente;

public interface DesbloquearClienteUseCase {

    Cliente peloCliente(String clienteId, String pin);

    Cliente peloGerente(String clienteId);
}
