package com.orbitapay.clientes.entity;

import java.math.BigDecimal;

public record Comprovante(Lancamento lancamento, BigDecimal novoSaldo, String protocolo) {
}
