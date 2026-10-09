package com.orbitapay.carteira.application.dto;

import java.math.BigDecimal;

public record OrdemRecebida(String ordemId, String clienteId, String ticker, int quantidade, BigDecimal precoUnitario) {
}
