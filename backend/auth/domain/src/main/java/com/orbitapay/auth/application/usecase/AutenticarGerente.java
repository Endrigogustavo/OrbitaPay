package com.orbitapay.auth.application.usecase;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import com.orbitapay.auth.application.dto.TokenDeAcesso;
import com.orbitapay.auth.application.service.EmissorDeToken;
import com.orbitapay.auth.domain.exception.CredenciaisInvalidasException;

public class AutenticarGerente {

    private final String codigoDoGerente;
    private final EmissorDeToken emissor;

    public AutenticarGerente(String codigoDoGerente, EmissorDeToken emissor) {
        this.codigoDoGerente = codigoDoGerente;
        this.emissor = emissor;
    }

    public TokenDeAcesso executar(String codigo) {
        if (codigo == null || !MessageDigest.isEqual(codigo.getBytes(StandardCharsets.UTF_8),
                codigoDoGerente.getBytes(StandardCharsets.UTF_8))) {
            throw new CredenciaisInvalidasException("Código de gerente inválido");
        }
        return emissor.emitirSessaoDeGerente();
    }
}
