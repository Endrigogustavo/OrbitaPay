package com.orbitapay.pagamentos.adapter.controller.dto.request;

import java.math.BigDecimal;

public record SolicitacaoDeDepositoRequest(BigDecimal valor, String metodo) {
}
