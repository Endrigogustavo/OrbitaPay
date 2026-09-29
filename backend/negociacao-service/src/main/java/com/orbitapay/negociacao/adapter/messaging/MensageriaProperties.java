package com.orbitapay.negociacao.adapter.messaging;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("mensageria")
public record MensageriaProperties(
        String exchange,
        String exchangeMorta,
        String filaMorta,
        String chaveMorta,
        String filaDeConsultasDeAtivos,
        Map<String, String> topicos,
        Map<String, Assinatura> assinaturas) {

    public MensageriaProperties {
        topicos = topicos == null ? Map.of() : Map.copyOf(topicos);
        assinaturas = assinaturas == null ? Map.of() : Map.copyOf(assinaturas);
    }

    public String topico(String nome) {
        String topico = topicos.get(nome);
        if (topico == null) {
            throw new IllegalStateException("Tópico não configurado: " + nome);
        }
        return topico;
    }

    public record Assinatura(String fila, String topico) {
    }
}
