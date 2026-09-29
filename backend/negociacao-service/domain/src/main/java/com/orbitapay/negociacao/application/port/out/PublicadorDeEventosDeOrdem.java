package com.orbitapay.negociacao.application.port.out;

import com.orbitapay.negociacao.domain.event.EventoDeOrdem;

public interface PublicadorDeEventosDeOrdem {

    void publicar(EventoDeOrdem evento);
}
