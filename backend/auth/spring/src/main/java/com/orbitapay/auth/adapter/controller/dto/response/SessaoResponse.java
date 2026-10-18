package com.orbitapay.auth.adapter.controller.dto.response;

import java.time.Instant;

import com.orbitapay.auth.entity.Sessao;

public record SessaoResponse(Instant expiraEm, String perfil, String clienteId, String nome) {

    public static SessaoResponse criarAPartirDe(Sessao sessao) {
        return new SessaoResponse(sessao.expiraEm(), sessao.perfil().name(), sessao.clienteId(), sessao.nome());
    }
}
