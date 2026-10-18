package com.orbitapay.clientes.adapter.gateway.http;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.orbitapay.clientes.application.gateway.ICredencialGateway;

@Component
public class CredencialHttpGateway extends GatewayHttpBase implements ICredencialGateway {

    public CredencialHttpGateway(RestClient authRestClient) {
        super(authRestClient, "de autenticação");
    }

    @Override
    public void atualizarEmailDeLogin(String clienteId, String novoEmail) {
        enviarPutParaOServico("/interno/credenciais/{clienteId}/email", Map.of("email", novoEmail), clienteId);
    }
}
