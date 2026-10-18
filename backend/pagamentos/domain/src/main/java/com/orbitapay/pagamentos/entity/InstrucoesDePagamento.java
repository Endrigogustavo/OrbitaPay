package com.orbitapay.pagamentos.entity;

import java.time.Instant;

public record InstrucoesDePagamento(String codigo, String descricao, Instant validoAte) {
}
