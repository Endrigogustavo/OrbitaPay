package com.orbitapay.clientes.application.port.in;

import com.orbitapay.clientes.domain.model.Cliente;
import com.orbitapay.clientes.domain.model.MotivoBloqueio;

public interface BloquearClienteUseCase {

    Cliente executar(String clienteId, MotivoBloqueio motivo);
}
