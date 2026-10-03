package com.orbitapay.auth.domain.model;

import java.time.Instant;

public class Credencial {

    public static final int LIMITE_TENTATIVAS_PIN = 3;

    private final String clienteId;
    private Email email;
    private String pinCodificado;
    private int tentativasFalhas;
    private final Instant criadaEm;

    public Credencial(String clienteId, Email email, String pinCodificado, int tentativasFalhas, Instant criadaEm) {
        this.clienteId = clienteId;
        this.email = email;
        this.pinCodificado = pinCodificado;
        this.tentativasFalhas = tentativasFalhas;
        this.criadaEm = criadaEm;
    }

    public static Credencial nova(String clienteId, Email email, String pinCodificado, Instant agora) {
        return new Credencial(clienteId, email, pinCodificado, 0, agora);
    }

    public boolean registrarPinIncorreto() {
        tentativasFalhas++;
        return tentativasFalhas == LIMITE_TENTATIVAS_PIN;
    }

    public void registrarPinCorreto() {
        tentativasFalhas = 0;
    }

    public void zerarTentativas() {
        tentativasFalhas = 0;
    }

    public void alterarPin(String novoPinCodificado) {
        this.pinCodificado = novoPinCodificado;
    }

    public void alterarEmail(Email email) {
        this.email = email;
    }

    public boolean limiteAtingido() {
        return tentativasFalhas >= LIMITE_TENTATIVAS_PIN;
    }

    public int tentativasRestantes() {
        return Math.max(0, LIMITE_TENTATIVAS_PIN - tentativasFalhas);
    }

    public String clienteId() {
        return clienteId;
    }

    public Email email() {
        return email;
    }

    public String pinCodificado() {
        return pinCodificado;
    }

    public int tentativasFalhas() {
        return tentativasFalhas;
    }

    public Instant criadaEm() {
        return criadaEm;
    }
}
