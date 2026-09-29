package com.orbitapay.ativos.application.port.in;

import com.orbitapay.ativos.application.dto.AtivoCotado;

public interface AtualizarAtivoUseCase {

    AtivoCotado executar(ListarAtivoNaBolsaUseCase.Comando comando);
}
