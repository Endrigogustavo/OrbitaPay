package com.orbitapay.gateway.domain;

public interface VerificadorDeCredencial {

    Credencial verificar(String token);
}
