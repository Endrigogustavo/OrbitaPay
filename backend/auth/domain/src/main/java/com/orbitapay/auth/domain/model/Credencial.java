package com.orbitapay.auth.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Credencial de acesso de um cliente: o e-mail de login, o PIN (só o hash) e o controle de tentativas.
 * Os dados cadastrais (nome, CPF) e a situação da conta pertencem ao contexto de Clientes.
 */
public class Credencial {

    public static final int LIMITE_TENTATIVAS_PIN = 3;

    private final String clienteId;
    private Email email;
    private String pinCodificado;
    private int tentativasFalhas;
    private final Instant criadaEm;

    private Credencial(String clienteId, Email email, String pinCodificado, int tentativasFalhas, Instant criadaEm) {
        this.clienteId = Objects.requireNonNull(clienteId);
        this.email = Objects.requireNonNull(email);
        this.pinCodificado = Objects.requireNonNull(pinCodificado);
        this.tentativasFalhas = tentativasFalhas;
        this.criadaEm = Objects.requireNonNull(criadaEm);
    }

    public static Credencial nova(String clienteId, Email email, String pinCodificado, Instant agora) {
        return new Credencial(clienteId, email, pinCodificado, 0, agora);
    }

    public static Credencial reconstituir(String clienteId, Email email, String pinCodificado, int tentativasFalhas,
            Instant criadaEm) {
        return new Credencial(clienteId, email, pinCodificado, tentativasFalhas, criadaEm);
    }

    /** Retorna true apenas na tentativa que atinge o limite, para o bloqueio ser anunciado uma única vez. */
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
        this.pinCodificado = Objects.requireNonNull(novoPinCodificado);
    }

    public void alterarEmail(Email email) {
        this.email = Objects.requireNonNull(email);
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
