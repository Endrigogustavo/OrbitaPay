package com.orbitapay.ativos.application.service;

import com.orbitapay.ativos.domain.event.EventoDeAtivo;

public interface PublicadorDeEventosDeAtivo {

    void publicar(EventoDeAtivo evento);
}
