package com.orbitapay.gateway.adapter.gateway.http;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.orbitapay.gateway.entity.Acesso;
import com.orbitapay.gateway.entity.Perfil;
import com.orbitapay.gateway.application.exception.AutenticacaoIndisponivelException;
import com.orbitapay.gateway.application.gateway.IAutenticacaoGateway;
import com.orbitapay.gateway.adapter.gateway.http.dto.response.AcessoResponse;

@Component
public class AutenticacaoHttpGateway implements IAutenticacaoGateway {

    private final RestClient auth;

    public AutenticacaoHttpGateway(RestClient authRestClient) {
        this.auth = authRestClient;
    }

    @Override
    public Optional<Acesso> consultarAcessoDaSessao(String tokenDeSessao, String tokenDeAssinatura) {
        try {
            AcessoResponse resposta = auth.get()
                    .uri("/interno/acessos")
                    .header("X-Sessao", tokenDeSessao)
                    .headers(cabecalhos -> {
                        if (tokenDeAssinatura != null) {
                            cabecalhos.set("X-Assinatura", tokenDeAssinatura);
                        }
                    })
                    .retrieve()
                    .body(AcessoResponse.class);
            return Optional.of(new Acesso(Perfil.valueOf(resposta.perfil()), resposta.clienteId(),
                    resposta.assinaturaValida()));
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return Optional.empty();
            }
            throw new AutenticacaoIndisponivelException(e);
        } catch (RestClientException e) {
            throw new AutenticacaoIndisponivelException(e);
        }
    }
}
