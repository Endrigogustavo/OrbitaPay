package com.orbitapay.auth.service;

import java.util.Locale;

import com.orbitapay.auth.entity.Funcionario;
import com.orbitapay.auth.entity.Perfil;
import com.orbitapay.auth.exception.RegraDeNegocioException;

public class CredencialService {

    public String validarENormalizarEmail(String email) {
        if (email == null || !email.trim().matches("^\\S+@\\S+\\.\\S+$")) {
            throw new RegraDeNegocioException("E-mail inválido");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public void validarFormatoDoPin(String pin) {
        if (pin == null || !pin.matches("\\d{4}")) {
            throw new RegraDeNegocioException("O PIN tem 4 dígitos");
        }
    }

    public void validarTrocaDePin(String pinAtual, String novoPin) {
        validarFormatoDoPin(novoPin);
        if (novoPin.equals(pinAtual)) {
            throw new RegraDeNegocioException("Escolha um PIN diferente do atual");
        }
    }

    public Funcionario criarFuncionario(String funcionarioId, String nome, String email, String pinCodificado,
            Perfil perfil) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("Informe o nome do funcionário");
        }
        if (perfil == Perfil.CLIENTE) {
            throw new RegraDeNegocioException("Funcionário é gerente ou admin");
        }
        return new Funcionario(funcionarioId, nome.trim(), validarENormalizarEmail(email), pinCodificado, perfil);
    }
}
