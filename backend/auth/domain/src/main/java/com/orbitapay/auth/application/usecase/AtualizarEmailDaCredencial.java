package com.orbitapay.auth.application.usecase;

import java.util.Optional;

import com.orbitapay.auth.domain.model.Credencial;
import com.orbitapay.auth.domain.model.Email;
import com.orbitapay.auth.domain.repository.CredencialRepository;

public class AtualizarEmailDaCredencial {

    private final CredencialRepository repositorio;

    public AtualizarEmailDaCredencial(CredencialRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void executar(String clienteId, String email) {
        Optional<Credencial> credencial = repositorio.buscarPorCliente(clienteId);
        if (credencial.isPresent()) {
            credencial.get().alterarEmail(new Email(email));
            repositorio.salvar(credencial.get());
        }
    }
}
