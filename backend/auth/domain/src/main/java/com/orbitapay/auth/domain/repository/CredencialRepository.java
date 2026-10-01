package com.orbitapay.auth.domain.repository;

import java.util.Optional;

import com.orbitapay.auth.domain.model.Credencial;
import com.orbitapay.auth.domain.model.Email;

public interface CredencialRepository {

    Optional<Credencial> buscarPorCliente(String clienteId);

    Optional<Credencial> buscarPorEmail(Email email);

    boolean emailEmUsoPorOutroCliente(Email email, String clienteId);

    void salvar(Credencial credencial);

    void remover(String clienteId);
}
