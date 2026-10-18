package com.orbitapay.clientes.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record Conta(String id, String clienteId, String agencia, String numero, BigDecimal saldo,
        List<Lancamento> lancamentos, Instant abertaEm) {
}
