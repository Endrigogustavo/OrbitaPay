package com.orbitapay.carteira.application.service;

import com.orbitapay.carteira.domain.event.EventoDeCarteira;

public interface PublicadorDeEventosDeCarteira {

    void publicar(EventoDeCarteira evento);
}
