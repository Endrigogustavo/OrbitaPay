package com.orbitapay.contas.application.port.in;

import java.math.BigDecimal;

public interface LiquidarOrdemUseCase {

    void debitarCompra(Comando comando);

    void creditarVenda(Comando comando);

    record Comando(String ordemId, String clienteId, String ticker, int quantidade, BigDecimal valorTotal) {
    }
}
