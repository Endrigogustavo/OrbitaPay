package com.orbitapay.pagamentos.domain.event;

import java.time.Instant;

import com.orbitapay.pagamentos.domain.model.Pagamento;

public sealed interface EventoDePagamento permits PagamentoConfirmado, PagamentoExpirado {

    Pagamento pagamento();

    Instant ocorridoEm();
}
