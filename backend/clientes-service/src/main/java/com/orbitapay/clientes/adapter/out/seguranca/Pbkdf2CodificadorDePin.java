package com.orbitapay.clientes.adapter.out.seguranca;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.stereotype.Component;

import com.orbitapay.clientes.application.port.out.CodificadorDePin;
import com.orbitapay.clientes.domain.model.Pin;

@Component
public class Pbkdf2CodificadorDePin implements CodificadorDePin {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACOES = 60_000;
    private static final int TAMANHO_CHAVE = 256;

    private final SecureRandom aleatorio = new SecureRandom();

    @Override
    public String codificar(Pin pin) {
        byte[] sal = new byte[16];
        aleatorio.nextBytes(sal);
        byte[] hash = derivar(pin.valor(), sal, ITERACOES);
        Base64.Encoder base64 = Base64.getEncoder();
        return ITERACOES + "$" + base64.encodeToString(sal) + "$" + base64.encodeToString(hash);
    }

    @Override
    public boolean confere(Pin pin, String pinCodificado) {
        String[] partes = pinCodificado.split("\\$");
        if (partes.length != 3) {
            return false;
        }
        Base64.Decoder base64 = Base64.getDecoder();
        byte[] esperado = base64.decode(partes[2]);
        byte[] calculado = derivar(pin.valor(), base64.decode(partes[1]), Integer.parseInt(partes[0]));
        return MessageDigest.isEqual(esperado, calculado);
    }

    private static byte[] derivar(String pin, byte[] sal, int iteracoes) {
        try {
            PBEKeySpec especificacao = new PBEKeySpec(pin.toCharArray(), sal, iteracoes, TAMANHO_CHAVE);
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(especificacao).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao codificar PIN", e);
        }
    }
}
