package com.orbitapay.carteira.application.port.out;

import com.orbitapay.carteira.domain.event.EventoDeCarteira;

public interface PublicadorDeEventosDeCarteira {

    void publicar(EventoDeCarteira evento);
}
