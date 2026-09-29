package com.orbitapay.contas.application.port.in;

import java.math.BigDecimal;

public interface AbrirContaUseCase {

    void executar(Comando comando);

    record Comando(String clienteId, String nomeTitular, BigDecimal depositoInicial) {
    }
}
