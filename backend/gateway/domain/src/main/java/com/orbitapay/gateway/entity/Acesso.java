package com.orbitapay.gateway.entity;

public record Acesso(Perfil perfil, String clienteId, boolean assinaturaValida) {
}
