package com.orbitapay.auth.application.usecase;

import java.util.UUID;

import com.orbitapay.auth.application.service.CodificadorDePin;
import com.orbitapay.auth.domain.exception.RegraDeNegocioException;
import com.orbitapay.auth.domain.model.Email;
import com.orbitapay.auth.domain.model.Funcionario;
import com.orbitapay.auth.domain.model.Perfil;
import com.orbitapay.auth.domain.model.Pin;
import com.orbitapay.auth.domain.repository.CredencialRepository;
import com.orbitapay.auth.domain.repository.FuncionarioRepository;

public class CadastrarFuncionario {

    public record Comando(String nome, String email, String pin, Perfil perfil) {
    }

    private final FuncionarioRepository funcionarios;
    private final CredencialRepository credenciais;
    private final CodificadorDePin codificador;

    public CadastrarFuncionario(FuncionarioRepository funcionarios, CredencialRepository credenciais,
            CodificadorDePin codificador) {
        this.funcionarios = funcionarios;
        this.credenciais = credenciais;
        this.codificador = codificador;
    }

    public Funcionario executar(Comando comando) {
        Email email = new Email(comando.email());
        Pin pin = new Pin(comando.pin());
        if (funcionarios.existePorEmail(email) || credenciais.buscarPorEmail(email).isPresent()) {
            throw new RegraDeNegocioException("Este e-mail já tem conta");
        }
        Funcionario funcionario = new Funcionario(UUID.randomUUID().toString(), comando.nome(), email,
                codificador.codificar(pin), comando.perfil());
        funcionarios.salvar(funcionario);
        return funcionario;
    }
}
