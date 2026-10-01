package com.orbitapay.auth.web.dto;

import java.time.Instant;

import com.orbitapay.auth.application.usecase.AutenticarCliente.Sessao;

public record SessaoResponse(String token, Instant expiraEm, String clienteId) {

    public static SessaoResponse de(Sessao sessao) {
        return new SessaoResponse(sessao.token().token(), sessao.token().expiraEm(), sessao.clienteId());
    }
}
