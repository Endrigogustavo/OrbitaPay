package com.orbitapay.pagamentos.adapter.controller.dto.response;

import java.util.Map;

public record ErroResponse(String codigo, String mensagem, Map<String, Object> detalhes) {
}
