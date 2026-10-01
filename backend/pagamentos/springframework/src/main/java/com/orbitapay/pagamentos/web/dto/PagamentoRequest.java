package com.orbitapay.pagamentos.web.dto;

import java.math.BigDecimal;

public record PagamentoRequest(BigDecimal valor, String metodo) {
}
