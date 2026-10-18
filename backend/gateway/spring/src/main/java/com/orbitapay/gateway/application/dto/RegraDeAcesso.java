package com.orbitapay.gateway.application.dto;

import com.orbitapay.gateway.entity.NivelDeAcesso;

public record RegraDeAcesso(String metodo, String caminho, NivelDeAcesso nivel) {
}
