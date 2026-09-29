package com.orbitapay.clientes.adapter.in.web.dto;

import java.time.Instant;

import com.orbitapay.clientes.application.port.in.AutenticarClienteUseCase.Sessao;

public record SessaoResponse(String token, Instant expiraEm, ClienteResponse cliente) {

    public static SessaoResponse de(Sessao sessao) {
        return new SessaoResponse(sessao.credencial().token(), sessao.credencial().expiraEm(),
                ClienteResponse.de(sessao.cliente()));
    }
}
