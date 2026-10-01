package com.orbitapay.pagamentos.application.service;

import com.orbitapay.pagamentos.domain.event.EventoDePagamento;

public interface PublicadorDeEventosDePagamento {

    void publicar(EventoDePagamento evento);
}
