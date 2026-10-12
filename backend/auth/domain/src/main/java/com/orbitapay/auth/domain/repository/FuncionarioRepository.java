package com.orbitapay.auth.domain.repository;

import java.util.Optional;

import com.orbitapay.auth.domain.model.Email;
import com.orbitapay.auth.domain.model.Funcionario;

public interface FuncionarioRepository {

    Optional<Funcionario> buscarPorEmail(Email email);

    boolean existePorEmail(Email email);

    void salvar(Funcionario funcionario);
}
