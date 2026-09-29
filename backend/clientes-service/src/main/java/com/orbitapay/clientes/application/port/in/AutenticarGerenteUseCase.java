package com.orbitapay.clientes.application.port.in;

import com.orbitapay.clientes.application.dto.Credencial;

public interface AutenticarGerenteUseCase {

    Credencial executar(String codigo);
}
