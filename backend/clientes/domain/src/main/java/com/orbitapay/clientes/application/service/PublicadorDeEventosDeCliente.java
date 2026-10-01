package com.orbitapay.clientes.application.service;

import com.orbitapay.clientes.domain.event.EventoDeCliente;

public interface PublicadorDeEventosDeCliente {

    void publicar(EventoDeCliente evento);
}
