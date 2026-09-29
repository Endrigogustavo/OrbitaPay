package com.orbitapay.negociacao.application.port.in;

import java.util.List;

import com.orbitapay.negociacao.domain.model.AtivoNegociavel;
import com.orbitapay.negociacao.domain.model.Ordem;

public interface ConsultarOrdensUseCase {

    Ordem buscarDoCliente(String ordemId, String clienteId);

    List<Ordem> listarDoCliente(String clienteId);

    AtivoNegociavel oferta(String ticker);
}
