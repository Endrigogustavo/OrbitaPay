package com.orbitapay.pagamentos.acl;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("orbita.acl")
public record AclProperties(Pix pix, Boleto boleto, Ted ted) {

    public record Pix(String chave, int expiracaoSegundos) {
    }

    public record Boleto(int diasParaVencimento) {
    }

    public record Ted(int horasDeValidade) {
    }
}
