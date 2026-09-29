package com.orbitapay.negociacao.application.port.in;

import com.orbitapay.negociacao.domain.model.Ordem;

public interface EnviarOrdemUseCase {

    Ordem executar(Comando comando);

    record Comando(String clienteId, String ticker, String tipo, int quantidade) {
    }
}
