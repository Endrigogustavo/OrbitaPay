package com.orbitapay.carteira.application.port.out;

import java.util.Optional;

import com.orbitapay.carteira.domain.model.AtivoCotado;

public interface CatalogoDeAtivos {

    Optional<AtivoCotado> consultar(String ticker);
}
