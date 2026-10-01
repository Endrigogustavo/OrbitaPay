package com.orbitapay.auth.application.service;

import com.orbitapay.auth.domain.model.Pin;

public interface CodificadorDePin {

    String codificar(Pin pin);

    boolean confere(Pin pin, String pinCodificado);
}
