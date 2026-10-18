package com.orbitapay.auth.adapter.controller.dto.response;

import java.util.Map;

public record ErroResponse(String codigo, String mensagem, Map<String, Object> detalhes) {
}
