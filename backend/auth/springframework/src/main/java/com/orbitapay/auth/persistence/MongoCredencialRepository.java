package com.orbitapay.auth.persistence;

import java.util.Optional;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import com.orbitapay.auth.domain.exception.RegraDeNegocioException;
import com.orbitapay.auth.domain.model.Credencial;
import com.orbitapay.auth.domain.model.Email;
import com.orbitapay.auth.domain.repository.CredencialRepository;

@Component
public class MongoCredencialRepository implements CredencialRepository {

    private final CredencialDocumentRepository mongo;

    public MongoCredencialRepository(CredencialDocumentRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Optional<Credencial> buscarPorCliente(String clienteId) {
        return mongo.findById(clienteId).map(MongoCredencialRepository::paraDominio);
    }

    @Override
    public Optional<Credencial> buscarPorEmail(Email email) {
        return mongo.findByEmail(email.valor()).map(MongoCredencialRepository::paraDominio);
    }

    @Override
    public boolean emailEmUsoPorOutroCliente(Email email, String clienteId) {
        return mongo.existsByEmailAndClienteIdNot(email.valor(), clienteId);
    }

    @Override
    public void salvar(Credencial credencial) {
        try {
            mongo.save(new CredencialDocument(credencial.clienteId(), credencial.email().valor(),
                    credencial.pinCodificado(), credencial.tentativasFalhas(), credencial.criadaEm()));
        } catch (DuplicateKeyException e) {
            throw new RegraDeNegocioException("Este e-mail já tem conta");
        }
    }

    @Override
    public void remover(String clienteId) {
        mongo.deleteById(clienteId);
    }

    private static Credencial paraDominio(CredencialDocument documento) {
        return new Credencial(documento.clienteId(), new Email(documento.email()),
                documento.pinCodificado(), documento.tentativasFalhas(), documento.criadaEm());
    }
}
