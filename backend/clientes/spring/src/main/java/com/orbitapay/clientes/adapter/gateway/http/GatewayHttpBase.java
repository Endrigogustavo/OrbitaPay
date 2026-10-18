package com.orbitapay.clientes.adapter.gateway.http;

import java.util.Map;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.orbitapay.clientes.adapter.gateway.exception.ServicoIndisponivelException;
import com.orbitapay.clientes.exception.RegraDeNegocioException;

public abstract class GatewayHttpBase {

    private final RestClient restClient;
    private final String nomeDoServico;

    protected GatewayHttpBase(RestClient restClient, String nomeDoServico) {
        this.restClient = restClient;
        this.nomeDoServico = nomeDoServico;
    }

    protected void enviarPutParaOServico(String caminho, Object corpo, Object... variaveisDoCaminho) {
        try {
            restClient.put().uri(caminho, variaveisDoCaminho).body(corpo).retrieve().toBodilessEntity();
        } catch (RestClientException erro) {
            throw traduzirErroDoServico(erro);
        }
    }

    private RuntimeException traduzirErroDoServico(RestClientException erro) {
        if (erro instanceof HttpClientErrorException operacaoRecusada) {
            return new RegraDeNegocioException(lerMensagemDoCorpoDoErro(operacaoRecusada));
        }
        return new ServicoIndisponivelException(nomeDoServico, erro);
    }

    private static String lerMensagemDoCorpoDoErro(HttpClientErrorException erro) {
        try {
            Map<?, ?> corpo = erro.getResponseBodyAs(Map.class);
            if (corpo != null && corpo.get("mensagem") != null) {
                return corpo.get("mensagem").toString();
            }
        } catch (RuntimeException corpoIlegivel) {
            return "Operação recusada";
        }
        return "Operação recusada";
    }
}
