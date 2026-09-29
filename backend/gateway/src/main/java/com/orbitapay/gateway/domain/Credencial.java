package com.orbitapay.gateway.domain;

import java.time.Instant;

public record Credencial(TipoDeCredencial tipo, String sujeito, Perfil perfil, Instant expiraEm) {

    public boolean validaEm(Instant instante) {
        return expiraEm.isAfter(instante);
    }

    public boolean sessaoDe(Perfil esperado) {
        return tipo == TipoDeCredencial.SESSAO && perfil == esperado;
    }

    public boolean assinaturaDe(String clienteId) {
        return tipo == TipoDeCredencial.ASSINATURA && sujeito.equals(clienteId);
    }
}
