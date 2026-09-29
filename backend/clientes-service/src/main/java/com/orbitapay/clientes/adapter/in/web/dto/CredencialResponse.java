package com.orbitapay.clientes.adapter.in.web.dto;

import java.time.Instant;

import com.orbitapay.clientes.application.dto.Credencial;

public record CredencialResponse(String token, Instant expiraEm) {

    public static CredencialResponse de(Credencial credencial) {
        return new CredencialResponse(credencial.token(), credencial.expiraEm());
    }
}
