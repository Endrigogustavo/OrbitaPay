package com.orbitapay.ativos.application.port.out;

import com.orbitapay.ativos.domain.event.EventoDeAtivo;

public interface PublicadorDeEventosDeAtivo {

    void publicar(EventoDeAtivo evento);
}
