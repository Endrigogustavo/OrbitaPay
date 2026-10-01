package com.orbitapay.gateway.domain;

import java.util.Optional;

public interface VerificadorDeCredencial {

    Optional<Credencial> verificar(String token);
}
