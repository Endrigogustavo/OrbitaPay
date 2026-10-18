package com.orbitapay.auth.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

import org.springframework.stereotype.Component;

import com.orbitapay.auth.adapter.interfaces.IGeradorDeHash;

@Component
public class GeradorDeHashSha256 implements IGeradorDeHash {

    @Override
    public String gerarHashDoToken(String token) {
        try {
            byte[] resumo = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resumo);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}
