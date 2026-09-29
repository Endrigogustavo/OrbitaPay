package com.orbitapay.gateway.application.port.out;

import java.util.Optional;

import com.orbitapay.gateway.domain.Credencial;

public interface VerificadorDeCredencial {

    Optional<Credencial> verificar(String token);
}
