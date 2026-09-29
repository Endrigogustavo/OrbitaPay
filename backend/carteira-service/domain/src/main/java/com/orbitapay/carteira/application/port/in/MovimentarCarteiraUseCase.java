package com.orbitapay.carteira.application.port.in;

import java.math.BigDecimal;

public interface MovimentarCarteiraUseCase {

    void reservarParaVenda(Comando comando);

    void liquidarCompra(Comando comando);

    void liquidarVenda(Comando comando);

    void cancelarVenda(Comando comando);

    void encerrar(String clienteId);

    record Comando(String ordemId, String clienteId, String ticker, int quantidade, BigDecimal precoUnitario) {
    }
}
