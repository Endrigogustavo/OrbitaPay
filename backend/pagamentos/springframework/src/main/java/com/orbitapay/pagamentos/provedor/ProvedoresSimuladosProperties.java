package com.orbitapay.pagamentos.provedor;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("orbita.provedores-simulados")
public record ProvedoresSimuladosProperties(Pix pix, Boleto boleto, Ted ted) {

    public record Pix(String ispb, int liquidacaoSegundos) {
    }

    public record Boleto(String codigoDoBanco, int liquidacaoSegundos) {
    }

    public record Ted(String bancoFavorecido, String agenciaFavorecida, String contaFavorecida, String nomeFavorecido,
            int liquidacaoSegundos) {
    }
}
