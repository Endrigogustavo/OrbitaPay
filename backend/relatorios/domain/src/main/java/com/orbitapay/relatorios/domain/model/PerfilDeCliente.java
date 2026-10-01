package com.orbitapay.relatorios.domain.model;

import java.time.Instant;
import java.util.Objects;

/** Projeção mínima do cliente para dar nome aos rankings e contar a base ativa e bloqueada. */
public class PerfilDeCliente {

    private final String clienteId;
    private String nome;
    private boolean bloqueado;
    private boolean ativo;
    private final Instant cadastradoEm;

    public PerfilDeCliente(String clienteId, String nome, boolean bloqueado, boolean ativo, Instant cadastradoEm) {
        this.clienteId = Objects.requireNonNull(clienteId);
        this.nome = nome;
        this.bloqueado = bloqueado;
        this.ativo = ativo;
        this.cadastradoEm = cadastradoEm;
    }

    public static PerfilDeCliente cadastrado(String clienteId, String nome, Instant quando) {
        return new PerfilDeCliente(clienteId, nome, false, true, quando);
    }

    public void renomear(String nome) {
        if (nome != null && !nome.isBlank()) {
            this.nome = nome;
        }
    }

    public void alterarSituacao(boolean bloqueado) {
        this.bloqueado = bloqueado;
    }

    public void encerrar() {
        this.ativo = false;
    }

    public String clienteId() {
        return clienteId;
    }

    public String nome() {
        return nome;
    }

    public boolean bloqueado() {
        return bloqueado;
    }

    public boolean ativo() {
        return ativo;
    }

    public Instant cadastradoEm() {
        return cadastradoEm;
    }
}
