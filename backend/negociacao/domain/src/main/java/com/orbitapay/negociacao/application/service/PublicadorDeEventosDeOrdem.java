package com.orbitapay.negociacao.application.service;

import com.orbitapay.negociacao.domain.event.EventoDeOrdem;

public interface PublicadorDeEventosDeOrdem {

    void publicar(EventoDeOrdem evento);
}
