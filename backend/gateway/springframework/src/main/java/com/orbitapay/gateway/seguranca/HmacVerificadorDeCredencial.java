package com.orbitapay.gateway.seguranca;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.orbitapay.gateway.domain.Credencial;
import com.orbitapay.gateway.domain.Perfil;
import com.orbitapay.gateway.domain.TipoDeCredencial;
import com.orbitapay.gateway.domain.VerificadorDeCredencial;

@Component
public class HmacVerificadorDeCredencial implements VerificadorDeCredencial {

    private static final String ALGORITMO = "HmacSHA256";

    private final byte[] segredo;

    public HmacVerificadorDeCredencial(@Value("${orbita.seguranca.segredo}") String segredo) {
        this.segredo = segredo.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public Credencial verificar(String token) {
        String[] partes = token.split("\\.");
        if (partes.length != 2) {
            return null;
        }
        try {
            byte[] assinaturaRecebida = Base64.getUrlDecoder().decode(partes[1]);
            if (!MessageDigest.isEqual(assinar(partes[0]), assinaturaRecebida)) {
                return null;
            }
            String[] campos = new String(Base64.getUrlDecoder().decode(partes[0]), StandardCharsets.UTF_8).split("\\|");
            if (campos.length != 4) {
                return null;
            }
            return new Credencial(TipoDeCredencial.valueOf(campos[0]), campos[1], Perfil.valueOf(campos[2]),
                    Instant.ofEpochSecond(Long.parseLong(campos[3])));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private byte[] assinar(String corpo) {
        try {
            Mac mac = Mac.getInstance(ALGORITMO);
            mac.init(new SecretKeySpec(segredo, ALGORITMO));
            return mac.doFinal(corpo.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao verificar credencial", e);
        }
    }
}
