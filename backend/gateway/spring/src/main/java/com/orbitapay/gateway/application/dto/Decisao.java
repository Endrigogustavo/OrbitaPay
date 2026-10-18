package com.orbitapay.gateway.application.dto;

import com.orbitapay.gateway.entity.Acesso;

public record Decisao(boolean liberada, int status, String codigo, String mensagem, Acesso acesso) {
}
