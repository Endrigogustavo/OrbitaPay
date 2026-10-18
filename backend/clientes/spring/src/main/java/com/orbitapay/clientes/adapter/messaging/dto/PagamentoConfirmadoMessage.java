package com.orbitapay.clientes.adapter.messaging.dto;

import java.math.BigDecimal;

public record PagamentoConfirmadoMessage(String pagamentoId, String clienteId, BigDecimal valor, String metodo) {
}
