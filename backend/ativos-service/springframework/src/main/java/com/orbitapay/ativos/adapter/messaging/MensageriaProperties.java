package com.orbitapay.ativos.adapter.messaging;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("mensageria")
public record MensageriaProperties(String exchange, String filaDeConsultas, Map<String, String> topicos) {

    public MensageriaProperties {
        topicos = topicos == null ? Map.of() : Map.copyOf(topicos);
    }

    public String topico(String nome) {
        String topico = topicos.get(nome);
        if (topico == null) {
            throw new IllegalStateException("Tópico não configurado: " + nome);
        }
        return topico;
    }
}
