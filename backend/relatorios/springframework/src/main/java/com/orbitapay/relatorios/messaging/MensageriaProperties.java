package com.orbitapay.relatorios.messaging;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("mensageria")
public record MensageriaProperties(
        String exchange,
        String exchangeMorta,
        String filaMorta,
        String chaveMorta,
        Map<String, Assinatura> assinaturas) {

    public MensageriaProperties {
        assinaturas = assinaturas == null ? Map.of() : Map.copyOf(assinaturas);
    }

    public record Assinatura(String fila, String topico) {
    }
}
