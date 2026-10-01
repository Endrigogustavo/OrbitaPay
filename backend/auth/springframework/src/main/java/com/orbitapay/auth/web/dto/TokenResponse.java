package com.orbitapay.auth.web.dto;

import java.time.Instant;

import com.orbitapay.auth.application.dto.TokenDeAcesso;

public record TokenResponse(String token, Instant expiraEm) {

    public static TokenResponse de(TokenDeAcesso token) {
        return new TokenResponse(token.token(), token.expiraEm());
    }
}
