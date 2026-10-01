package com.orbitapay.auth.application.service;

import com.orbitapay.auth.application.dto.TokenDeAcesso;

public interface EmissorDeToken {

    TokenDeAcesso emitirSessaoDeCliente(String clienteId);

    TokenDeAcesso emitirSessaoDeGerente();

    TokenDeAcesso emitirAssinatura(String clienteId);
}
