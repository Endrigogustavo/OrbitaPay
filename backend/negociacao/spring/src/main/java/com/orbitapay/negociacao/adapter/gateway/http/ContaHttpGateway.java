package com.orbitapay.negociacao.adapter.gateway.http;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.orbitapay.negociacao.application.gateway.IContaGateway;

@Component
public class ContaHttpGateway extends GatewayHttpBase implements IContaGateway {

    public ContaHttpGateway(RestClient clientesRestClient) {
        super(clientesRestClient, "de contas");
    }

    @Override
    public void debitarCompraDeAcoes(String clienteId, String ordemId, BigDecimal valor, String descricao) {
        enviarPostParaOServico("/interno/contas/{clienteId}/compras",
                Map.of("ordemId", ordemId, "valor", valor, "descricao", descricao), clienteId);
    }

    @Override
    public void estornarCompraDeAcoes(String clienteId, String ordemId) {
        enviarPostParaOServico("/interno/contas/{clienteId}/compras/{ordemId}/estorno", null, clienteId, ordemId);
    }

    @Override
    public void creditarVendaDeAcoes(String clienteId, String ordemId, BigDecimal valor, String descricao) {
        enviarPostParaOServico("/interno/contas/{clienteId}/vendas",
                Map.of("ordemId", ordemId, "valor", valor, "descricao", descricao), clienteId);
    }
}
