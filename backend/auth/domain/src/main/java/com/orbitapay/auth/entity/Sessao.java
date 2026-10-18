package com.orbitapay.auth.entity;

import java.time.Instant;

public record Sessao(String token, Perfil perfil, String sujeito, String clienteId, String nome, Instant expiraEm) {
}
