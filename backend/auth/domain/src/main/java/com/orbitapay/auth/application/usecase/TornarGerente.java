package com.orbitapay.auth.application.usecase;

import java.util.UUID;

import com.orbitapay.auth.application.dto.Sessao;
import com.orbitapay.auth.application.service.EmissorDeToken;
import com.orbitapay.auth.domain.exception.CredencialNaoEncontradaException;
import com.orbitapay.auth.domain.model.Credencial;
import com.orbitapay.auth.domain.model.Funcionario;
import com.orbitapay.auth.domain.model.Perfil;
import com.orbitapay.auth.domain.repository.CredencialRepository;
import com.orbitapay.auth.domain.repository.FuncionarioRepository;

public class TornarGerente {

    private final CredencialRepository credenciais;
    private final FuncionarioRepository funcionarios;
    private final EmissorDeToken emissor;

    public TornarGerente(CredencialRepository credenciais, FuncionarioRepository funcionarios,
            EmissorDeToken emissor) {
        this.credenciais = credenciais;
        this.funcionarios = funcionarios;
        this.emissor = emissor;
    }

    public Sessao executar(String clienteId, String nome) {
        Credencial credencial = credenciais.buscarPorCliente(clienteId)
                .orElseThrow(() -> new CredencialNaoEncontradaException(clienteId));
        Funcionario gerente = new Funcionario(UUID.randomUUID().toString(), nome == null ? null : nome.trim(),
                credencial.email(), credencial.pinCodificado(), Perfil.GERENTE);
        funcionarios.salvar(gerente);
        credenciais.remover(clienteId);
        return new Sessao(Perfil.GERENTE, null, gerente.nome(),
                emissor.emitirSessaoDeFuncionario(gerente.id(), Perfil.GERENTE));
    }
}
