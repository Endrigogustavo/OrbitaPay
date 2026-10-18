package com.orbitapay.clientes.adapter.controller.dto.request;

import java.math.BigDecimal;

public record MovimentoDeOrdemRequest(String ordemId, BigDecimal valor, String descricao) {
}
