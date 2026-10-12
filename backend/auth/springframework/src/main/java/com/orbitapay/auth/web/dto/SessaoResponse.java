package com.orbitapay.auth.web.dto;

import java.time.Instant;

import com.orbitapay.auth.application.dto.Sessao;

public record SessaoResponse(String token, Instant expiraEm, String perfil, String clienteId, String nome) {

    public static SessaoResponse de(Sessao sessao) {
        return new SessaoResponse(sessao.token().token(), sessao.token().expiraEm(), sessao.perfil().name(),
                sessao.clienteId(), sessao.nome());
    }
}
