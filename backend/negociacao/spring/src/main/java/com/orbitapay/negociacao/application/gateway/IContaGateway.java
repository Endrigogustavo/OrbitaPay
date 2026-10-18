package com.orbitapay.negociacao.application.gateway;

import java.math.BigDecimal;

public interface IContaGateway {

    void debitarCompraDeAcoes(String clienteId, String ordemId, BigDecimal valor, String descricao);

    void estornarCompraDeAcoes(String clienteId, String ordemId);

    void creditarVendaDeAcoes(String clienteId, String ordemId, BigDecimal valor, String descricao);
}
