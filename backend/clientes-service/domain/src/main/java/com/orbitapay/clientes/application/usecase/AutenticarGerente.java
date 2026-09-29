package com.orbitapay.clientes.application.usecase;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

import com.orbitapay.clientes.application.dto.Credencial;
import com.orbitapay.clientes.application.port.in.AutenticarGerenteUseCase;
import com.orbitapay.clientes.application.port.out.EmissorDeCredencial;
import com.orbitapay.clientes.domain.exception.CredenciaisInvalidasException;

public class AutenticarGerente implements AutenticarGerenteUseCase {

    private final String codigoDoGerente;
    private final EmissorDeCredencial emissor;

    public AutenticarGerente(String codigoDoGerente, EmissorDeCredencial emissor) {
        this.codigoDoGerente = codigoDoGerente;
        this.emissor = emissor;
    }

    @Override
    public Credencial executar(String codigo) {
        if (codigo == null || !MessageDigest.isEqual(codigo.getBytes(StandardCharsets.UTF_8),
                codigoDoGerente.getBytes(StandardCharsets.UTF_8))) {
            throw new CredenciaisInvalidasException("Código de gerente inválido");
        }
        return emissor.emitirSessaoDeGerente();
    }
}
