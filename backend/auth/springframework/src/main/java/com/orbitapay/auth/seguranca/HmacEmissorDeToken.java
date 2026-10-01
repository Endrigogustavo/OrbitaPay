package com.orbitapay.auth.seguranca;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.orbitapay.auth.application.dto.TokenDeAcesso;
import com.orbitapay.auth.application.service.EmissorDeToken;

/**
 * Emite tokens no formato {@code base64url(tipo|sujeito|perfil|expiraEm).base64url(hmacSha256)}. O gateway
 * valida a assinatura com o mesmo segredo, sem precisar consultar este serviço a cada requisição.
 */
@Component
public class HmacEmissorDeToken implements EmissorDeToken {

    private static final String ALGORITMO = "HmacSHA256";

    private final byte[] segredo;
    private final Duration duracaoSessao;
    private final Duration duracaoAssinatura;
    private final Clock relogio;

    public HmacEmissorDeToken(@Value("${orbita.seguranca.segredo}") String segredo,
            @Value("${orbita.seguranca.sessao-minutos}") long sessaoMinutos,
            @Value("${orbita.seguranca.assinatura-segundos}") long assinaturaSegundos,
            Clock relogio) {
        this.segredo = segredo.getBytes(StandardCharsets.UTF_8);
        this.duracaoSessao = Duration.ofMinutes(sessaoMinutos);
        this.duracaoAssinatura = Duration.ofSeconds(assinaturaSegundos);
        this.relogio = relogio;
    }

    @Override
    public TokenDeAcesso emitirSessaoDeCliente(String clienteId) {
        return emitir("SESSAO", clienteId, "CLIENTE", duracaoSessao);
    }

    @Override
    public TokenDeAcesso emitirSessaoDeGerente() {
        return emitir("SESSAO", "gerente", "GERENTE", duracaoSessao);
    }

    @Override
    public TokenDeAcesso emitirAssinatura(String clienteId) {
        return emitir("ASSINATURA", clienteId, "CLIENTE", duracaoAssinatura);
    }

    private TokenDeAcesso emitir(String tipo, String sujeito, String perfil, Duration duracao) {
        Instant expiraEm = Instant.now(relogio).plus(duracao);
        String conteudo = String.join("|", tipo, sujeito, perfil, String.valueOf(expiraEm.getEpochSecond()));
        Base64.Encoder base64 = Base64.getUrlEncoder().withoutPadding();
        String corpo = base64.encodeToString(conteudo.getBytes(StandardCharsets.UTF_8));
        return new TokenDeAcesso(corpo + "." + base64.encodeToString(assinar(corpo)), expiraEm);
    }

    private byte[] assinar(String corpo) {
        try {
            Mac mac = Mac.getInstance(ALGORITMO);
            mac.init(new SecretKeySpec(segredo, ALGORITMO));
            return mac.doFinal(corpo.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao assinar token", e);
        }
    }
}
