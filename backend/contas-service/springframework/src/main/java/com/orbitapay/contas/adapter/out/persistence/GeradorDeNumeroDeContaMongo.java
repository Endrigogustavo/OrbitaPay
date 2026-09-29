package com.orbitapay.contas.adapter.out.persistence;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

import com.orbitapay.contas.application.port.out.GeradorDeNumeroDeConta;

@Component
public class GeradorDeNumeroDeContaMongo implements GeradorDeNumeroDeConta {

    private final ContaMongoRepository mongo;
    private final SecureRandom aleatorio = new SecureRandom();

    public GeradorDeNumeroDeContaMongo(ContaMongoRepository mongo) {
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
