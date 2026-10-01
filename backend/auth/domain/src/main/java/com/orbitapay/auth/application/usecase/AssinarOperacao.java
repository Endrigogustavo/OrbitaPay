package com.orbitapay.auth.application.usecase;

import com.orbitapay.auth.application.dto.TokenDeAcesso;
import com.orbitapay.auth.application.service.EmissorDeToken;
import com.orbitapay.auth.domain.exception.CredencialNaoEncontradaException;
import com.orbitapay.auth.domain.model.Credencial;
import com.orbitapay.auth.domain.model.Pin;
import com.orbitapay.auth.domain.repository.CredencialRepository;

/**
 * Emite a assinatura de curta duração que o gateway exige em saques, ordens e no desbloqueio pelo cliente.
 * A assinatura só prova que o cliente conhece o PIN: cada contexto continua aplicando as próprias regras
 * (Contas recusa saque de titular bloqueado, Negociação recusa ordem de investidor bloqueado).
 */
public class AssinarOperacao {

    private final CredencialRepository repositorio;
    private final ConferenciaDePin conferencia;
    private final EmissorDeToken emissor;

    public AssinarOperacao(CredencialRepository repositorio, ConferenciaDePin conferencia, EmissorDeToken emissor) {
        this.repositorio = repositorio;
        this.conferencia = conferencia;
        this.emissor = emissor;
    }

    public TokenDeAcesso executar(String clienteId, String pin) {
        Credencial credencial = repositorio.buscarPorCliente(clienteId)
                .orElseThrow(() -> new CredencialNaoEncontradaException(clienteId));
        conferencia.conferir(credencial, new Pin(pin));
        return emissor.emitirAssinatura(credencial.clienteId());
    }
}
