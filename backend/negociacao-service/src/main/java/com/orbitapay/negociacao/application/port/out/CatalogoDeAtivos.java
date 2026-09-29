package com.orbitapay.negociacao.application.port.out;

import java.util.Optional;

import com.orbitapay.negociacao.application.dto.AtivoDoCatalogo;

public interface CatalogoDeAtivos {

    Optional<AtivoDoCatalogo> consultar(String ticker);
}
