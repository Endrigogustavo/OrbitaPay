package com.orbitapay.auth.application.gateway;

import java.util.Optional;

import com.orbitapay.auth.entity.Funcionario;

public interface IFuncionarioGateway {

    void salvar(Funcionario funcionario);

    Optional<Funcionario> buscarPorEmail(String email);

    boolean existePorEmail(String email);
}
