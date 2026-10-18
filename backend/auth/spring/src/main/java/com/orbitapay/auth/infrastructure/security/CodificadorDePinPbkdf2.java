package com.orbitapay.auth.infrastructure.security;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.stereotype.Component;

import com.orbitapay.auth.application.gateway.ICodificadorDePinGateway;

@Component
public class CodificadorDePinPbkdf2 implements ICodificadorDePinGateway {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACOES = 60_000;
    private static final int TAMANHO_DA_CHAVE = 256;

    private final SecureRandom geradorAleatorio = new SecureRandom();

    @Override
    public String codificarPin(String pin) {
        byte[] sal = new byte[16];
        geradorAleatorio.nextBytes(sal);
        Base64.Encoder base64 = Base64.getEncoder();
        return ITERACOES + "$" + base64.encodeToString(sal) + "$"
                + base64.encodeToString(derivarChaveDoPin(pin, sal, ITERACOES));
    }

    @Override
    public boolean pinConfereComOCodificado(String pin, String pinCodificado) {
        String[] partesDoPinCodificado = pinCodificado.split("\\$");
        if (partesDoPinCodificado.length != 3) {
            return false;
        }
        Base64.Decoder base64 = Base64.getDecoder();
        int iteracoes = Integer.parseInt(partesDoPinCodificado[0]);
        byte[] sal = base64.decode(partesDoPinCodificado[1]);
        byte[] chaveEsperada = base64.decode(partesDoPinCodificado[2]);
        return MessageDigest.isEqual(chaveEsperada, derivarChaveDoPin(pin, sal, iteracoes));
    }

    private static byte[] derivarChaveDoPin(String pin, byte[] sal, int iteracoes) {
        try {
            PBEKeySpec especificacao = new PBEKeySpec(pin.toCharArray(), sal, iteracoes, TAMANHO_DA_CHAVE);
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(especificacao).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao codificar o PIN", e);
        }
    }
}
