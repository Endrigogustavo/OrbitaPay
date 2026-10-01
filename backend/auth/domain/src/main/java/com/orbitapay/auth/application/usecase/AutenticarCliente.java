package com.orbitapay.auth.application.usecase;

import com.orbitapay.auth.application.dto.TokenDeAcesso;
import com.orbitapay.auth.application.service.EmissorDeToken;
import com.orbitapay.auth.domain.exception.CredenciaisInvalidasException;
import com.orbitapay.auth.domain.model.Credencial;
import com.orbitapay.auth.domain.model.Email;
import com.orbitapay.auth.domain.model.Pin;
import com.orbitapay.auth.domain.repository.CredencialRepository;

public class AutenticarCliente {

    public record Sessao(String clienteId, TokenDeAcesso token) {
    }

    private final CredencialRepository repositorio;
    private final ConferenciaDePin conferencia;
    private final EmissorDeToken emissor;

    public AutenticarCliente(CredencialRepository repositorio, ConferenciaDePin conferencia, EmissorDeToken emissor) {
        this.repositorio = repositorio;
        this.conferencia = conferencia;
        this.emissor = emissor;
    }

    public Sessao executar(String email, String pin) {
        if (email == null || email.isBlank()) {
            throw new CredenciaisInvalidasException("Informe seu e-mail");
        }
        Credencial credencial = repositorio.buscarPorEmail(new Email(email))
                .orElseThrow(() -> new CredenciaisInvalidasException("E-mail não encontrado"));
        conferencia.conferir(credencial, new Pin(pin));
        return new Sessao(credencial.clienteId(), emissor.emitirSessaoDeCliente(credencial.clienteId()));
    }
}
