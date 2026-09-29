package com.orbitapay.clientes.application.port.out;

import com.orbitapay.clientes.domain.model.Pin;

public interface CodificadorDePin {

    String codificar(Pin pin);

    boolean confere(Pin pin, String pinCodificado);
}
