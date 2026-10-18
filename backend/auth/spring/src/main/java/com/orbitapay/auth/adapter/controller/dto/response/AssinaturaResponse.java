package com.orbitapay.auth.adapter.controller.dto.response;

import java.time.Instant;

import com.orbitapay.auth.entity.Assinatura;

public record AssinaturaResponse(String token, Instant expiraEm) {

    public static AssinaturaResponse criarAPartirDe(Assinatura assinatura) {
        return new AssinaturaResponse(assinatura.token(), assinatura.expiraEm());
    }
}
