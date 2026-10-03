package com.orbitapay.clientes.domain.model;

import java.time.Instant;

import com.orbitapay.clientes.domain.exception.RegraDeNegocioException;

public class Cliente {

    private final String id;
    private NomeCompleto nome;
    private Email email;
    private Cpf cpf;
    private boolean bloqueado;
    private MotivoBloqueio motivoBloqueio;
    private final Instant clienteDesde;

    public Cliente(String id, NomeCompleto nome, Email email, Cpf cpf, boolean bloqueado,
            MotivoBloqueio motivoBloqueio, Instant clienteDesde) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.bloqueado = bloqueado;
        this.motivoBloqueio = motivoBloqueio;
        this.clienteDesde = clienteDesde;
    }

    public static Cliente novo(String id, NomeCompleto nome, Email email, Cpf cpf, Instant agora) {
        return new Cliente(id, nome, email, cpf, false, null, agora);
    }

    public void atualizarDados(NomeCompleto nome, Email email) {
        this.nome = nome;
        this.email = email;
    }

    public void corrigirCpf(Cpf cpf) {
        this.cpf = cpf;
    }

    public void bloquear(MotivoBloqueio motivo) {
        if (bloqueado) {
            throw new RegraDeNegocioException("A conta já está bloqueada");
        }
        this.bloqueado = true;
        this.motivoBloqueio = motivo;
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

    public boolean bloqueado() {
        return bloqueado;
    }

    public MotivoBloqueio motivoBloqueio() {
        return motivoBloqueio;
    }

    public Instant clienteDesde() {
        return clienteDesde;
    }
}
