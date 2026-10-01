package com.orbitapay.negociacao.domain.model;

import java.util.Objects;

import com.orbitapay.negociacao.domain.exception.InvestidorBloqueadoException;

public class Investidor {

    private final String clienteId;
    private boolean bloqueado;

    public Investidor(String clienteId, boolean bloqueado) {
        this.clienteId = Objects.requireNonNull(clienteId);
        this.bloqueado = bloqueado;
    }

    public void alterarSituacao(boolean bloqueado) {
        this.bloqueado = bloqueado;
    }

    public void exigirLiberado() {
        if (bloqueado) {
            throw new InvestidorBloqueadoException();
        }
    }

    public String clienteId() {
        return clienteId;
    }

    public boolean bloqueado() {
        return bloqueado;
    }
}
