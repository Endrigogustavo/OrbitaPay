package com.orbitapay.negociacao.infrastructure.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient ativosRestClient(@Value("${orbita.servicos.ativos}") String urlDoServicoDeAtivos) {
        return criarRestClient(urlDoServicoDeAtivos);
    }

    @Bean
    public RestClient clientesRestClient(@Value("${orbita.servicos.clientes}") String urlDoServicoDeClientes) {
        return criarRestClient(urlDoServicoDeClientes);
    }

    private static RestClient criarRestClient(String urlBase) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(2));
        fabrica.setReadTimeout(Duration.ofSeconds(10));
        return RestClient.builder().baseUrl(urlBase).requestFactory(fabrica).build();
    }
}
