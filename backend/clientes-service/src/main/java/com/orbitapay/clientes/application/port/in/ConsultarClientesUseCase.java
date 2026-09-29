package com.orbitapay.clientes.application.port.in;

import java.util.List;

import com.orbitapay.clientes.domain.model.Cliente;

public interface ConsultarClientesUseCase {

    Cliente buscar(String clienteId);

    List<Cliente> listar();
}
