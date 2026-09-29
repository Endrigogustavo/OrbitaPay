package com.orbitapay.clientes.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.orbitapay.clientes.domain.exception.RegraDeNegocioException;

public class Cliente {

    public static final int LIMITE_TENTATIVAS_PIN = 3;

    private final String id;
    private NomeCompleto nome;
    private Email email;
    private Cpf cpf;
    private String pinCodificado;
    private boolean bloqueado;
    private MotivoBloqueio motivoBloqueio;
    private int tentativasFalhas;
    private final Instant clienteDesde;

    private Cliente(String id, NomeCompleto nome, Email email, Cpf cpf, String pinCodificado, boolean bloqueado,
            MotivoBloqueio motivoBloqueio, int tentativasFalhas, Instant clienteDesde) {
        this.id = Objects.requireNonNull(id);
        this.nome = Objects.requireNonNull(nome);
        this.email = Objects.requireNonNull(email);
        this.cpf = Objects.requireNonNull(cpf);
        this.pinCodificado = Objects.requireNonNull(pinCodificado);
        this.bloqueado = bloqueado;
        this.motivoBloqueio = motivoBloqueio;
        this.tentativasFalhas = tentativasFalhas;
        this.clienteDesde = Objects.requireNonNull(clienteDesde);
    }

    public static Cliente novo(String id, NomeCompleto nome, Email email, Cpf cpf, String pinCodificado, Instant agora) {
        return new Cliente(id, nome, email, cpf, pinCodificado, false, null, 0, agora);
    }

    public static Cliente reconstituir(String id, NomeCompleto nome, Email email, Cpf cpf, String pinCodificado,
            boolean bloqueado, MotivoBloqueio motivoBloqueio, int tentativasFalhas, Instant clienteDesde) {
        return new Cliente(id, nome, email, cpf, pinCodificado, bloqueado, motivoBloqueio, tentativasFalhas,
                clienteDesde);
    }

    public void atualizarDados(NomeCompleto nome, Email email) {
        this.nome = Objects.requireNonNull(nome);
        this.email = Objects.requireNonNull(email);
    }

    public void corrigirCpf(Cpf cpf) {
        this.cpf = Objects.requireNonNull(cpf);
    }

    public void alterarPin(String novoPinCodificado) {
        this.pinCodificado = Objects.requireNonNull(novoPinCodificado);
    }

    public boolean registrarPinIncorreto() {
        tentativasFalhas++;
        if (tentativasFalhas >= LIMITE_TENTATIVAS_PIN && !bloqueado) {
            bloquear(MotivoBloqueio.PIN);
            return true;
        }
        return false;
    }

    public void registrarPinCorreto() {
        tentativasFalhas = 0;
    }

    public void bloquear(MotivoBloqueio motivo) {
        if (bloqueado) {
            throw new RegraDeNegocioException("A conta já está bloqueada");
        }
        this.bloqueado = true;
        this.motivoBloqueio = Objects.requireNonNull(motivo);
    }

    public void desbloquearPeloCliente() {
        exigirBloqueio();
        if (motivoBloqueio != MotivoBloqueio.CLIENTE) {
            throw new RegraDeNegocioException("Somente o gerente pode desbloquear esta conta");
        }
        liberar();
    }

    public void desbloquearPeloGerente() {
        exigirBloqueio();
        liberar();
    }

    private void exigirBloqueio() {
        if (!bloqueado) {
            throw new RegraDeNegocioException("A conta não está bloqueada");
        }
    }

    private void liberar() {
        this.bloqueado = false;
        this.motivoBloqueio = null;
        this.tentativasFalhas = 0;
    }

    public int tentativasRestantes() {
        return Math.max(0, LIMITE_TENTATIVAS_PIN - tentativasFalhas);
    }

    public String id() {
        return id;
    }

    public NomeCompleto nome() {
        return nome;
    }

    public Email email() {
        return email;
    }

    public Cpf cpf() {
        return cpf;
    }

    public String pinCodificado() {
        return pinCodificado;
    }

    public boolean bloqueado() {
        return bloqueado;
    }

    public MotivoBloqueio motivoBloqueio() {
        return motivoBloqueio;
    }

    public int tentativasFalhas() {
        return tentativasFalhas;
    }

    public Instant clienteDesde() {
        return clienteDesde;
    }
}
