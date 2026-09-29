package com.orbitapay.clientes.application.port.out;

import com.orbitapay.clientes.domain.event.EventoDeCliente;

public interface PublicadorDeEventosDeCliente {

    void publicar(EventoDeCliente evento);
}
