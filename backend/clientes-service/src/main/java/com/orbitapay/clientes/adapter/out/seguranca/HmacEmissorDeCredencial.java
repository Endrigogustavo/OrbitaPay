package com.orbitapay.clientes.adapter.out.seguranca;

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

import com.orbitapay.clientes.application.dto.Credencial;
import com.orbitapay.clientes.application.port.out.EmissorDeCredencial;

@Component
public class HmacEmissorDeCredencial implements EmissorDeCredencial {

    private static final String ALGORITMO = "HmacSHA256";

    private final byte[] segredo;
    private final Duration duracaoSessao;
    private final Duration duracaoAssinatura;
    private final Clock relogio;

    public HmacEmissorDeCredencial(@Value("${orbita.seguranca.segredo}") String segredo,
            @Value("${orbita.seguranca.sessao-minutos}") long sessaoMinutos,
            @Value("${orbita.seguranca.assinatura-segundos}") long assinaturaSegundos,
            Clock relogio) {
        this.segredo = segredo.getBytes(StandardCharsets.UTF_8);
        this.duracaoSessao = Duration.ofMinutes(sessaoMinutos);
        this.duracaoAssinatura = Duration.ofSeconds(assinaturaSegundos);
        this.relogio = relogio;
    }

    @Override
    public Credencial emitirSessaoDeCliente(String clienteId) {
        return emitir("SESSAO", clienteId, "CLIENTE", duracaoSessao);
    }

    @Override
    public Credencial emitirSessaoDeGerente() {
        return emitir("SESSAO", "gerente", "GERENTE", duracaoSessao);
    }

    @Override
    public Credencial emitirAssinatura(String clienteId) {
        return emitir("ASSINATURA", clienteId, "CLIENTE", duracaoAssinatura);
    }

    private Credencial emitir(String tipo, String sujeito, String perfil, Duration duracao) {
        Instant expiraEm = Instant.now(relogio).plus(duracao);
        String conteudo = String.join("|", tipo, sujeito, perfil, String.valueOf(expiraEm.getEpochSecond()));
        Base64.Encoder base64 = Base64.getUrlEncoder().withoutPadding();
        String corpo = base64.encodeToString(conteudo.getBytes(StandardCharsets.UTF_8));
        return new Credencial(corpo + "." + base64.encodeToString(assinar(corpo)), expiraEm);
    }

    private byte[] assinar(String corpo) {
        try {
            Mac mac = Mac.getInstance(ALGORITMO);
            mac.init(new SecretKeySpec(segredo, ALGORITMO));
            return mac.doFinal(corpo.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao assinar credencial", e);
        }
    }
}
