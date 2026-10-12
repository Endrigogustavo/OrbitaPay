package com.orbitapay.auth.application.usecase;

import com.orbitapay.auth.application.dto.Sessao;
import com.orbitapay.auth.domain.exception.CredenciaisInvalidasException;
import com.orbitapay.auth.domain.model.Email;
import com.orbitapay.auth.domain.repository.FuncionarioRepository;

public class Entrar {

    private final FuncionarioRepository funcionarios;
    private final AutenticarFuncionario autenticarFuncionario;
    private final AutenticarCliente autenticarCliente;

    public Entrar(FuncionarioRepository funcionarios, AutenticarFuncionario autenticarFuncionario,
            AutenticarCliente autenticarCliente) {
        this.funcionarios = funcionarios;
        this.autenticarFuncionario = autenticarFuncionario;
        this.autenticarCliente = autenticarCliente;
    }

    public Sessao executar(String email, String pin) {
        if (email == null || email.isBlank()) {
            throw new CredenciaisInvalidasException("Informe seu e-mail");
        }
        Email conta = new Email(email);
        if (funcionarios.existePorEmail(conta)) {
            return autenticarFuncionario.executar(conta, pin);
        }
        return autenticarCliente.executar(conta, pin);
    }
}
