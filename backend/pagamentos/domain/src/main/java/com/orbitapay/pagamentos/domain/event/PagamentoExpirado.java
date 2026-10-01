package com.orbitapay.pagamentos.domain.event;

import java.time.Instant;

import com.orbitapay.pagamentos.domain.model.Pagamento;

public record PagamentoExpirado(Pagamento pagamento, Instant ocorridoEm) implements EventoDePagamento {
}
