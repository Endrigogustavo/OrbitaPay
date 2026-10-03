package com.orbitapay.pagamentos.domain.model;

import java.time.Instant;

public record InstrucoesDePagamento(String codigo, String descricao, Instant validoAte) {

}
