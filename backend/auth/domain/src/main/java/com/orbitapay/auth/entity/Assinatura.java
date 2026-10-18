package com.orbitapay.auth.entity;

import java.time.Instant;

public record Assinatura(String token, String clienteId, Instant expiraEm) {
}
