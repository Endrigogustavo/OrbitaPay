package com.orbitapay.auth.application.service;

import com.orbitapay.auth.application.dto.TokenDeAcesso;
import com.orbitapay.auth.domain.model.Perfil;

public interface EmissorDeToken {

    TokenDeAcesso emitirSessaoDeCliente(String clienteId);

    TokenDeAcesso emitirSessaoDeFuncionario(String funcionarioId, Perfil perfil);

    TokenDeAcesso emitirAssinatura(String clienteId);
}
