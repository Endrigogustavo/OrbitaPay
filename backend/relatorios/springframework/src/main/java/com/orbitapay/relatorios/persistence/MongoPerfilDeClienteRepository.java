package com.orbitapay.relatorios.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.orbitapay.relatorios.domain.model.PerfilDeCliente;
import com.orbitapay.relatorios.domain.repository.PerfilDeClienteRepository;

@Component
public class MongoPerfilDeClienteRepository implements PerfilDeClienteRepository {

    private final PerfilDeClienteDocumentRepository mongo;

    public MongoPerfilDeClienteRepository(PerfilDeClienteDocumentRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Optional<PerfilDeCliente> buscar(String clienteId) {
        return mongo.findById(clienteId).map(MongoPerfilDeClienteRepository::paraDominio);
    }

    @Override
    public List<PerfilDeCliente> listar() {
        return mongo.findAll().stream().map(MongoPerfilDeClienteRepository::paraDominio).toList();
    }

    @Override
    public void salvar(PerfilDeCliente perfil) {
        mongo.save(new PerfilDeClienteDocument(perfil.clienteId(), perfil.nome(), perfil.bloqueado(), perfil.ativo(),
                perfil.cadastradoEm()));
    }

    private static PerfilDeCliente paraDominio(PerfilDeClienteDocument documento) {
        return new PerfilDeCliente(documento.clienteId(), documento.nome(), documento.bloqueado(), documento.ativo(),
                documento.cadastradoEm());
    }
}
