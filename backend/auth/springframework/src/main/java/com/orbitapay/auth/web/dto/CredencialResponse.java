package com.orbitapay.auth.web.dto;

import com.orbitapay.auth.domain.model.Credencial;

public record CredencialResponse(String clienteId, String email, int tentativasFalhas, int tentativasPermitidas) {

    public static CredencialResponse de(Credencial credencial) {
        return new CredencialResponse(credencial.clienteId(), credencial.email().valor(),
                credencial.tentativasFalhas(), Credencial.LIMITE_TENTATIVAS_PIN);
    }
}
