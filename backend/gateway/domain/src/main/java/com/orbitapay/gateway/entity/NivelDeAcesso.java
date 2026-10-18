package com.orbitapay.gateway.entity;

public enum NivelDeAcesso {
    PUBLICO,
    SESSAO_OPCIONAL,
    AUTENTICADO,
    CLIENTE,
    CLIENTE_COM_ASSINATURA,
    GERENTE,
    ADMIN,
    NEGADO
}
