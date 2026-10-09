package com.orbitapay.relatorios.application.usecase;

import java.util.Optional;

import com.orbitapay.relatorios.domain.model.PerfilDeCliente;
import com.orbitapay.relatorios.domain.repository.PerfilDeClienteRepository;

public class RenomearCliente {

    private final PerfilDeClienteRepository perfis;

    public RenomearCliente(PerfilDeClienteRepository perfis) {
        this.perfis = perfis;
    }

    public void executar(String clienteId, String nome) {
        Optional<PerfilDeCliente> perfil = perfis.buscar(clienteId);
        if (perfil.isPresent()) {
            perfil.get().renomear(nome);
            perfis.salvar(perfil.get());
        }
    }
}
