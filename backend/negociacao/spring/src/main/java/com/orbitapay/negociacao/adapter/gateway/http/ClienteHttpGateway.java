package com.orbitapay.negociacao.adapter.gateway.http;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.orbitapay.negociacao.adapter.gateway.http.dto.response.SituacaoDoClienteResponse;
import com.orbitapay.negociacao.application.gateway.IClienteGateway;

@Component
public class ClienteHttpGateway extends GatewayHttpBase implements IClienteGateway {

    public ClienteHttpGateway(RestClient clientesRestClient) {
        super(clientesRestClient, "de clientes");
    }

    @Override
    public boolean clienteEstaBloqueado(String clienteId) {
        return buscarNoServico(SituacaoDoClienteResponse.class, "/interno/clientes/{clienteId}/situacao", clienteId)
                .map(SituacaoDoClienteResponse::bloqueado)
                .orElse(false);
    }
}
