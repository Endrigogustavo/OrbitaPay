package com.orbitapay.auth.application.usecase;

import com.orbitapay.auth.application.dto.Sessao;
import com.orbitapay.auth.application.service.CodificadorDePin;
import com.orbitapay.auth.application.service.EmissorDeToken;
import com.orbitapay.auth.domain.exception.CredenciaisInvalidasException;
import com.orbitapay.auth.domain.model.Email;
import com.orbitapay.auth.domain.model.Funcionario;
import com.orbitapay.auth.domain.model.Pin;
import com.orbitapay.auth.domain.repository.FuncionarioRepository;

public class AutenticarFuncionario {

    private final FuncionarioRepository repositorio;
    private final CodificadorDePin codificador;
    private final EmissorDeToken emissor;

    public AutenticarFuncionario(FuncionarioRepository repositorio, CodificadorDePin codificador,
            EmissorDeToken emissor) {
        this.repositorio = repositorio;
        this.codificador = codificador;
        this.emissor = emissor;
    }

    public Sessao executar(Email email, String pin) {
        Funcionario funcionario = repositorio.buscarPorEmail(email)
                .orElseThrow(() -> new CredenciaisInvalidasException("E-mail não encontrado"));
        if (!codificador.confere(new Pin(pin), funcionario.pinCodificado())) {
            throw new CredenciaisInvalidasException("PIN incorreto");
        }
        return new Sessao(funcionario.perfil(), null, funcionario.nome(),
                emissor.emitirSessaoDeFuncionario(funcionario.id(), funcionario.perfil()));
    }
}
