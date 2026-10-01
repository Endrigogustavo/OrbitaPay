package com.orbitapay.clientes.web.dto;

import java.util.Map;

public record ErroResponse(String codigo, String mensagem, Map<String, Object> detalhes) {

    public static ErroResponse de(String codigo, String mensagem) {
        return new ErroResponse(codigo, mensagem, Map.of());
    }
}
