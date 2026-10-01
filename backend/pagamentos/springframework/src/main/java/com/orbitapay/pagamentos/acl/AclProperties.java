package com.orbitapay.pagamentos.acl;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Parâmetros que o OrbitaPay envia a cada provedor (chave Pix do recebedor, prazos de validade). */
@ConfigurationProperties("orbita.acl")
public record AclProperties(Pix pix, Boleto boleto, Ted ted) {

    public record Pix(String chave, int expiracaoSegundos) {
    }

    public record Boleto(int diasParaVencimento) {
    }

    public record Ted(int horasDeValidade) {
    }
}
