package com.orbitapay.auth.application.gateway;

import java.util.Optional;

import com.orbitapay.auth.entity.Assinatura;

public interface IAssinaturaGateway {

    void salvar(Assinatura assinatura);

    Optional<Assinatura> buscarPorToken(String token);
}
