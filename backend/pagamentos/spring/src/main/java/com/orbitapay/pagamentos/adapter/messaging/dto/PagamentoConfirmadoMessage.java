package com.orbitapay.pagamentos.adapter.messaging.dto;

import java.math.BigDecimal;

public record PagamentoConfirmadoMessage(String pagamentoId, String clienteId, BigDecimal valor, String metodo) {
}
