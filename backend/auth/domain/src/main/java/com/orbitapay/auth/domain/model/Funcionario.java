package com.orbitapay.auth.domain.model;

import com.orbitapay.auth.domain.exception.RegraDeNegocioException;

public record Funcionario(String id, String nome, Email email, String pinCodificado, Perfil perfil) {

    public Funcionario {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("Informe o nome do funcionário");
        }
        if (perfil != Perfil.GERENTE && perfil != Perfil.ADMIN) {
            throw new RegraDeNegocioException("Funcionário é gerente ou admin");
        }
    }
}
