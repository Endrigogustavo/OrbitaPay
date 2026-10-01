package com.orbitapay.clientes.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.orbitapay.clientes.domain.exception.RegraDeNegocioException;

/**
 * Cadastro do cliente e situação da conta. O PIN e as tentativas de acesso pertencem ao contexto de
 * Autenticação, que avisa por evento quando o limite de tentativas é atingido.
 */
public class Cliente {

    private final String id;
    private NomeCompleto nome;
    private Email email;
    private Cpf cpf;
    private boolean bloqueado;
    private MotivoBloqueio motivoBloqueio;
    private final Instant clienteDesde;

    private Cliente(String id, NomeCompleto nome, Email email, Cpf cpf, boolean bloqueado,
            MotivoBloqueio motivoBloqueio, Instant clienteDesde) {
        this.id = Objects.requireNonNull(id);
        this.nome = Objects.requireNonNull(nome);
        this.email = Objects.requireNonNull(email);
        this.cpf = Objects.requireNonNull(cpf);
        this.bloqueado = bloqueado;
        this.motivoBloqueio = motivoBloqueio;
        this.clienteDesde = Objects.requireNonNull(clienteDesde);
    }

    public static Cliente novo(String id, NomeCompleto nome, Email email, Cpf cpf, Instant agora) {
        return new Cliente(id, nome, email, cpf, false, null, agora);
    }

    public static Cliente reconstituir(String id, NomeCompleto nome, Email email, Cpf cpf, boolean bloqueado,
            MotivoBloqueio motivoBloqueio, Instant clienteDesde) {
        return new Cliente(id, nome, email, cpf, bloqueado, motivoBloqueio, clienteDesde);
    }

    public void atualizarDados(NomeCompleto nome, Email email) {
        this.nome = Objects.requireNonNull(nome);
        this.email = Objects.requireNonNull(email);
    }

    public void corrigirCpf(Cpf cpf) {
        this.cpf = Objects.requireNonNull(cpf);
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
