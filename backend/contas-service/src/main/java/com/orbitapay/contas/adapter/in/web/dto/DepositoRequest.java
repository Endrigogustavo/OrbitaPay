package com.orbitapay.contas.adapter.in.web.dto;

import java.math.BigDecimal;

public record DepositoRequest(BigDecimal valor, String metodo) {
}
