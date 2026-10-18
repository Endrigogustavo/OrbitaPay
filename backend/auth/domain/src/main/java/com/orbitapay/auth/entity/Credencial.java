package com.orbitapay.auth.entity;

import java.time.Instant;

public record Credencial(String clienteId, String email, String pinCodificado, int tentativasFalhas, Instant criadaEm) {
}
