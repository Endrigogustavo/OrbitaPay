package com.orbitapay.auth.application.usecase;

import com.orbitapay.auth.application.dto.Sessao;
import com.orbitapay.auth.application.service.EmissorDeToken;
import com.orbitapay.auth.domain.exception.CredenciaisInvalidasException;
import com.orbitapay.auth.domain.model.Credencial;
import com.orbitapay.auth.domain.model.Email;
import com.orbitapay.auth.domain.model.Perfil;
import com.orbitapay.auth.domain.model.Pin;
import com.orbitapay.auth.domain.repository.CredencialRepository;

public class AutenticarCliente {

    private final CredencialRepository repositorio;
    private final ConferenciaDePin conferencia;
    private final EmissorDeToken emissor;

    public AutenticarCliente(CredencialRepository repositorio, ConferenciaDePin conferencia, EmissorDeToken emissor) {
        this.repositorio = repositorio;
        this.conferencia = conferencia;
        this.emissor = emissor;
    }

    public Sessao executar(Email email, String pin) {
        Credencial credencial = repositorio.buscarPorEmail(email)
                .orElseThrow(() -> new CredenciaisInvalidasException("E-mail não encontrado"));
        conferencia.conferir(credencial, new Pin(pin));
        return new Sessao(Perfil.CLIENTE, credencial.clienteId(), null,
                emissor.emitirSessaoDeCliente(credencial.clienteId()));
    }
}
