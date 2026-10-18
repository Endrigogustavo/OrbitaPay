package com.orbitapay.gateway.application.gateway;

import java.util.Optional;

import com.orbitapay.gateway.entity.Acesso;

public interface IAutenticacaoGateway {

    Optional<Acesso> consultarAcessoDaSessao(String tokenDeSessao, String tokenDeAssinatura);
}
