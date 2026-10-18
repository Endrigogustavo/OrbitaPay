package com.orbitapay.auth.adapter.gateway.http;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.orbitapay.auth.adapter.gateway.exception.ServicoIndisponivelException;
import com.orbitapay.auth.application.gateway.IClienteGateway;

@Component
public class ClienteHttpGateway implements IClienteGateway {

    private final RestClient clientesRestClient;

    public ClienteHttpGateway(RestClient clientesRestClient) {
        this.clientesRestClient = clientesRestClient;
    }

    @Override
    public void bloquearPorPin(String clienteId) {
        try {
            clientesRestClient.post()
                    .uri("/interno/clientes/{clienteId}/bloqueio-por-pin", clienteId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new ServicoIndisponivelException("de clientes", e);
        }
    }
}
