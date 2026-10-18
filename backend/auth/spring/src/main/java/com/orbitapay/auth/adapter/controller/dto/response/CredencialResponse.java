package com.orbitapay.auth.adapter.controller.dto.response;

import com.orbitapay.auth.entity.Credencial;
import com.orbitapay.auth.application.usecase.CredencialUseCase;

public record CredencialResponse(String clienteId, String email, int tentativasFalhas, int tentativasPermitidas) {

    public static CredencialResponse criarAPartirDe(Credencial credencial) {
        return new CredencialResponse(credencial.clienteId(), credencial.email(), credencial.tentativasFalhas(),
                CredencialUseCase.LIMITE_DE_TENTATIVAS_DE_PIN);
    }
}
