package com.orbitapay.clientes.application.port.out;

import com.orbitapay.clientes.application.dto.Credencial;

public interface EmissorDeCredencial {

    Credencial emitirSessaoDeCliente(String clienteId);

    Credencial emitirSessaoDeGerente();

    Credencial emitirAssinatura(String clienteId);
}
