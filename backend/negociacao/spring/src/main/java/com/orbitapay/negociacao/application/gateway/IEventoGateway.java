package com.orbitapay.negociacao.application.gateway;

import com.orbitapay.negociacao.entity.Ordem;

public interface IEventoGateway {

    void publicarOrdemSolicitada(Ordem ordem);
}
