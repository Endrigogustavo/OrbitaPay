package com.orbitapay.auth.persistence;

import java.util.Optional;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import com.orbitapay.auth.domain.exception.RegraDeNegocioException;
import com.orbitapay.auth.domain.model.Email;
import com.orbitapay.auth.domain.model.Funcionario;
import com.orbitapay.auth.domain.model.Perfil;
import com.orbitapay.auth.domain.repository.FuncionarioRepository;

@Repository
public class MongoFuncionarioRepository implements FuncionarioRepository {

    private final FuncionarioDocumentRepository mongo;

    public MongoFuncionarioRepository(FuncionarioDocumentRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Optional<Funcionario> buscarPorEmail(Email email) {
        return mongo.findByEmail(email.valor()).map(MongoFuncionarioRepository::paraDominio);
    }

    @Override
    public boolean existePorEmail(Email email) {
        return mongo.existsByEmail(email.valor());
    }

    @Override
    public void salvar(Funcionario funcionario) {
        try {
            mongo.save(new FuncionarioDocument(funcionario.id(), funcionario.nome(), funcionario.email().valor(),
                    funcionario.pinCodificado(), funcionario.perfil().name()));
        } catch (DuplicateKeyException e) {
            throw new RegraDeNegocioException("Este e-mail já tem conta");
        }
    }

    private static Funcionario paraDominio(FuncionarioDocument documento) {
        return new Funcionario(documento.id(), documento.nome(), new Email(documento.email()),
                documento.pinCodificado(), Perfil.valueOf(documento.perfil()));
    }
}
