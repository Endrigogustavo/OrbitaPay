package com.orbitapay.auth.application.usecase;

import java.time.Instant;

import com.orbitapay.auth.application.service.CodificadorDePin;
import com.orbitapay.auth.domain.exception.RegraDeNegocioException;
import com.orbitapay.auth.domain.model.Credencial;
import com.orbitapay.auth.domain.model.Email;
import com.orbitapay.auth.domain.model.Pin;
import com.orbitapay.auth.domain.repository.CredencialRepository;

public class RegistrarCredencial {

    public record Comando(String clienteId, String email, String pin) {
    }

    private final CredencialRepository repositorio;
    private final CodificadorDePin codificador;

    public RegistrarCredencial(CredencialRepository repositorio, CodificadorDePin codificador) {
        this.repositorio = repositorio;
        this.codificador = codificador;
    }

    public void executar(Comando comando) {
        if (comando.clienteId() == null || comando.clienteId().isBlank()) {
            throw new RegraDeNegocioException("Cliente não informado");
        }
        Email email = new Email(comando.email());
        Pin pin = new Pin(comando.pin());
        if (repositorio.buscarPorCliente(comando.clienteId()).isPresent()) {
            return;
        }
        if (repositorio.emailEmUsoPorOutroCliente(email, comando.clienteId())) {
            throw new RegraDeNegocioException("Este e-mail já tem conta");
        }
        repositorio.salvar(Credencial.nova(comando.clienteId(), email, codificador.codificar(pin),
                Instant.now()));
    }
}
