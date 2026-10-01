package com.orbitapay.auth.application.dto;

import java.time.Instant;

public record TokenDeAcesso(String token, Instant expiraEm) {
}
