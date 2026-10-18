package com.orbitapay.auth.adapter.gateway.mongo;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.orbitapay.auth.entity.Funcionario;
import com.orbitapay.auth.entity.Perfil;
import com.orbitapay.auth.application.gateway.IFuncionarioGateway;
import com.orbitapay.auth.adapter.gateway.mongo.document.FuncionarioDocument;
import com.orbitapay.auth.adapter.gateway.mongo.repository.FuncionarioRepository;

@Component
public class FuncionarioMongoGateway implements IFuncionarioGateway {

    private final FuncionarioRepository funcionarioRepository;

    public FuncionarioMongoGateway(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }

    @Override
    public void salvar(Funcionario funcionario) {
        funcionarioRepository.save(new FuncionarioDocument(funcionario.id(), funcionario.nome(), funcionario.email(),
                funcionario.pinCodificado(), funcionario.perfil().name()));
    }

    @Override
    public Optional<Funcionario> buscarPorEmail(String email) {
        return funcionarioRepository.findByEmail(email).map(documento -> new Funcionario(documento.id(),
                documento.nome(), documento.email(), documento.pinCodificado(), Perfil.valueOf(documento.perfil())));
    }

    @Override
    public boolean existePorEmail(String email) {
        return funcionarioRepository.existsByEmail(email);
    }
}
