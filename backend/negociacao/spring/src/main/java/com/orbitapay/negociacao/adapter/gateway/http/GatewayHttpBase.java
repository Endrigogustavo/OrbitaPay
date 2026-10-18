package com.orbitapay.negociacao.adapter.gateway.http;

import java.util.Map;
import java.util.Optional;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.orbitapay.negociacao.adapter.gateway.exception.ServicoIndisponivelException;
import com.orbitapay.negociacao.application.exception.OperacaoRecusadaException;

public abstract class GatewayHttpBase {

    private final RestClient restClient;
    private final String nomeDoServico;

    protected GatewayHttpBase(RestClient restClient, String nomeDoServico) {
        this.restClient = restClient;
        this.nomeDoServico = nomeDoServico;
    }

    protected <T> Optional<T> buscarNoServico(Class<T> tipoDaResposta, String caminho, Object... variaveisDoCaminho) {
        try {
            return Optional.ofNullable(restClient.get().uri(caminho, variaveisDoCaminho).retrieve().body(tipoDaResposta));
        } catch (HttpClientErrorException.NotFound naoEncontrado) {
            return Optional.empty();
        } catch (RestClientException erro) {
            throw traduzirErroDoServico(erro);
        }
    }

    protected <T> T buscarListaNoServico(ParameterizedTypeReference<T> tipoDaResposta, String caminho) {
        try {
            return restClient.get().uri(caminho).retrieve().body(tipoDaResposta);
        } catch (RestClientException erro) {
            throw traduzirErroDoServico(erro);
        }
    }

    protected void enviarPostParaOServico(String caminho, Object corpo, Object... variaveisDoCaminho) {
        try {
            RestClient.RequestBodySpec requisicao = restClient.post().uri(caminho, variaveisDoCaminho);
            if (corpo != null) {
                requisicao.body(corpo);
            }
            requisicao.retrieve().toBodilessEntity();
        } catch (RestClientException erro) {
            throw traduzirErroDoServico(erro);
        }
    }

    private RuntimeException traduzirErroDoServico(RestClientException erro) {
        if (erro instanceof HttpClientErrorException operacaoRecusada) {
            return new OperacaoRecusadaException(lerMensagemDoCorpoDoErro(operacaoRecusada));
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
