package com.orbitapay.clientes.application.dto;

import java.time.Instant;

public record Credencial(String token, Instant expiraEm) {
}
