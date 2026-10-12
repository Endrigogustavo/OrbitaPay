package com.orbitapay.auth.application.usecase;

import java.time.Duration;
import java.time.Instant;

import com.orbitapay.auth.domain.exception.CredencialNaoEncontradaException;
import com.orbitapay.auth.domain.exception.RegraDeNegocioException;
import com.orbitapay.auth.domain.model.Credencial;
import com.orbitapay.auth.domain.repository.CredencialRepository;

public class DesfazerCadastroDeCredencial {

    public static final Duration JANELA = Duration.ofMinutes(10);

    private final CredencialRepository repositorio;

    public DesfazerCadastroDeCredencial(CredencialRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void executar(String clienteId) {
        Credencial credencial = repositorio.buscarPorCliente(clienteId)
                .orElseThrow(() -> new CredencialNaoEncontradaException(clienteId));
        if (!credencial.criadaHaMenosDe(JANELA, Instant.now())) {
            throw new RegraDeNegocioException("O cadastro já foi concluído · para sair da Órbita, encerre a conta");
        }
        repositorio.remover(clienteId);
    }
}
