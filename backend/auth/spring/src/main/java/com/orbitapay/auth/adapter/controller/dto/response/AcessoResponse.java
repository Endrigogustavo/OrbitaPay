package com.orbitapay.auth.adapter.controller.dto.response;

import com.orbitapay.auth.entity.Acesso;

public record AcessoResponse(String perfil, String clienteId, boolean assinaturaValida) {

    public static AcessoResponse criarAPartirDe(Acesso acesso) {
        return new AcessoResponse(acesso.sessao().perfil().name(), acesso.sessao().clienteId(),
                acesso.assinaturaValida());
    }
}
