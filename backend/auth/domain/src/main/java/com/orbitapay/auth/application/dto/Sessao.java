package com.orbitapay.auth.application.dto;

import com.orbitapay.auth.domain.model.Perfil;

public record Sessao(Perfil perfil, String clienteId, String nome, TokenDeAcesso token) {
}
