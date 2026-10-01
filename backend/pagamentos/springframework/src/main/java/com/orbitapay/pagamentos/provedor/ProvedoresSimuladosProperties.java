package com.orbitapay.pagamentos.provedor;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Comportamento dos provedores simulados. {@code liquidacaoSegundos} é quanto o pagador leva para pagar depois
 * que a cobrança é emitida.
 */
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
