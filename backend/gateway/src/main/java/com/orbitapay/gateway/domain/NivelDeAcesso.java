package com.orbitapay.gateway.domain;

public enum NivelDeAcesso {
    PUBLICO,
    PUBLICO_COM_SESSAO_OPCIONAL,
    CLIENTE,
    CLIENTE_COM_ASSINATURA,
    GERENTE,
    NEGADO
}
