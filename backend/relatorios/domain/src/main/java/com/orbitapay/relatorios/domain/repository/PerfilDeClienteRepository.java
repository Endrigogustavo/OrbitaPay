package com.orbitapay.relatorios.domain.repository;

import java.util.List;
import java.util.Optional;

import com.orbitapay.relatorios.domain.model.PerfilDeCliente;

public interface PerfilDeClienteRepository {

    Optional<PerfilDeCliente> buscar(String clienteId);

    List<PerfilDeCliente> listar();

    void salvar(PerfilDeCliente perfil);
}
