package com.orbitapay.auth.application.gateway;

import java.util.Optional;

import com.orbitapay.auth.entity.Credencial;

public interface ICredencialGateway {

    void salvar(Credencial credencial);

    Optional<Credencial> buscarPorCliente(String clienteId);

    Optional<Credencial> buscarPorEmail(String email);

    boolean emailEmUsoPorOutroCliente(String email, String clienteId);

    void remover(String clienteId);
}
