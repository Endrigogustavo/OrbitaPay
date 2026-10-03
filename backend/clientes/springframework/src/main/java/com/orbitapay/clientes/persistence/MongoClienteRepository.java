package com.orbitapay.clientes.persistence;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.orbitapay.clientes.domain.exception.EmailJaCadastradoException;
import com.orbitapay.clientes.domain.model.Cliente;
import com.orbitapay.clientes.domain.model.Cpf;
import com.orbitapay.clientes.domain.model.Email;
import com.orbitapay.clientes.domain.model.MotivoBloqueio;
import com.orbitapay.clientes.domain.model.NomeCompleto;
import com.orbitapay.clientes.domain.repository.ClienteRepository;

@Component
public class MongoClienteRepository implements ClienteRepository {

    private final ClienteDocumentRepository mongo;

    public MongoClienteRepository(ClienteDocumentRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public String proximoId() {
        return new ObjectId().toHexString();
    }

    @Override
    public Optional<Cliente> buscarPorId(String clienteId) {
        return mongo.findById(clienteId).map(MongoClienteRepository::paraDominio);
    }

    @Override
    public Optional<Cliente> buscarPorEmail(Email email) {
        return mongo.findByEmail(email.valor()).map(MongoClienteRepository::paraDominio);
    }

    @Override
    public boolean existeEmail(Email email) {
        return mongo.existsByEmail(email.valor());
    }

    @Override
    public boolean existeEmailDeOutroCliente(Email email, String clienteId) {
        return mongo.existsByEmailAndIdNot(email.valor(), clienteId);
    }

    @Override
    public List<Cliente> listar() {
        return mongo.findAll(Sort.by("clienteDesde")).stream().map(MongoClienteRepository::paraDominio).toList();
    }

    @Override
    public boolean vazio() {
        return mongo.count() == 0;
    }

    @Override
    public void salvar(Cliente cliente) {
        try {
            mongo.save(paraDocumento(cliente));
        } catch (DuplicateKeyException e) {
            throw new EmailJaCadastradoException();
        }
    }

    @Override
    public void remover(String clienteId) {
        mongo.deleteById(clienteId);
    }

    private static ClienteDocument paraDocumento(Cliente cliente) {
        return new ClienteDocument(
                cliente.id(),
                cliente.nome().valor(),
                cliente.email().valor(),
                cliente.cpf().valor(),
                cliente.bloqueado(),
                cliente.motivoBloqueio() == null ? null : cliente.motivoBloqueio().name(),
                cliente.clienteDesde());
    }

    private static Cliente paraDominio(ClienteDocument documento) {
        return new Cliente(
                documento.id(),
                new NomeCompleto(documento.nome()),
                new Email(documento.email()),
                new Cpf(documento.cpf()),
                documento.bloqueado(),
                documento.motivoBloqueio() == null ? null : MotivoBloqueio.valueOf(documento.motivoBloqueio()),
                documento.clienteDesde());
    }
}
