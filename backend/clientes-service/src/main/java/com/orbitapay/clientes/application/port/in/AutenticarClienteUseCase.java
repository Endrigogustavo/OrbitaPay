package com.orbitapay.clientes.application.port.in;

import com.orbitapay.clientes.application.dto.Credencial;
import com.orbitapay.clientes.domain.model.Cliente;

public interface AutenticarClienteUseCase {

    Sessao executar(String email, String pin);

    record Sessao(Cliente cliente, Credencial credencial) {
    }
}
