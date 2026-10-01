package com.orbitapay.clientes.messaging;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("mensageria")
public record MensageriaProperties(
        String exchange,
        String exchangeMorta,
        String filaMorta,
        String chaveMorta,
        String filaDeRegistrosDeCredencial,
        Map<String, String> topicos,
        Map<String, Assinatura> assinaturas) {

    public MensageriaProperties {
        topicos = topicos == null ? Map.of() : Map.copyOf(topicos);
        assinaturas = assinaturas == null ? Map.of() : Map.copyOf(assinaturas);
    }

    public record Assinatura(String fila, String topico) {
    }
}
