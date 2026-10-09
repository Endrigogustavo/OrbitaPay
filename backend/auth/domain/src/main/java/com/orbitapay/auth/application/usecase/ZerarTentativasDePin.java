package com.orbitapay.auth.application.usecase;

import java.util.Optional;

import com.orbitapay.auth.domain.model.Credencial;
import com.orbitapay.auth.domain.repository.CredencialRepository;

public class ZerarTentativasDePin {

    private final CredencialRepository repositorio;

    public ZerarTentativasDePin(CredencialRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void executar(String clienteId) {
        Optional<Credencial> credencial = repositorio.buscarPorCliente(clienteId);
        if (credencial.isPresent()) {
            credencial.get().zerarTentativas();
            repositorio.salvar(credencial.get());
        }
    }
}
