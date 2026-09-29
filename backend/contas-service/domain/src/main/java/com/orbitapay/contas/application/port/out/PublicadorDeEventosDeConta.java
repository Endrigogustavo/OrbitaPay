package com.orbitapay.contas.application.port.out;

import com.orbitapay.contas.domain.event.EventoDeConta;

public interface PublicadorDeEventosDeConta {

    void publicar(EventoDeConta evento);
}
