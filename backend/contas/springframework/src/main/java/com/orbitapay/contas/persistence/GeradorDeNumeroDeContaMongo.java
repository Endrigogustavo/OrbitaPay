package com.orbitapay.contas.persistence;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

import com.orbitapay.contas.application.service.GeradorDeNumeroDeConta;

@Component
public class GeradorDeNumeroDeContaMongo implements GeradorDeNumeroDeConta {

    private final ContaDocumentRepository mongo;
    private final SecureRandom aleatorio = new SecureRandom();

    public GeradorDeNumeroDeContaMongo(ContaDocumentRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public String proximo() {
        String numero;
        do {
            numero = (10000 + aleatorio.nextInt(90000)) + "-" + aleatorio.nextInt(10);
        } while (mongo.existsByNumero(numero));
        return numero;
    }
}
