package com.orbitapay.ativos.application.port.in;

import java.util.List;
import java.util.Optional;

import com.orbitapay.ativos.application.dto.AtivoCotado;
import com.orbitapay.ativos.domain.model.Bolsa;

public interface ConsultarAtivosUseCase {

    List<AtivoCotado> listar();

    AtivoCotado buscar(String ticker);

    Optional<AtivoCotado> procurar(String ticker);

    List<Bolsa> bolsas();
}
