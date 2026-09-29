package com.orbitapay.clientes.application.port.in;

import com.orbitapay.clientes.application.dto.Credencial;

public interface AssinarOperacaoUseCase {

    Credencial executar(String clienteId, String pin);
}
