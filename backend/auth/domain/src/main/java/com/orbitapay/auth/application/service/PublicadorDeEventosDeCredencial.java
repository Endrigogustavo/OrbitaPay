package com.orbitapay.auth.application.service;

import com.orbitapay.auth.domain.event.EventoDeCredencial;

public interface PublicadorDeEventosDeCredencial {

    void publicar(EventoDeCredencial evento);
}
