package com.orbitapay.clientes.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.orbitapay.clientes.application.port.out.ClienteRepository;
import com.orbitapay.clientes.domain.exception.EmailJaCadastradoException;
import com.orbitapay.clientes.domain.model.Cliente;
import com.orbitapay.clientes.domain.model.Cpf;
import com.orbitapay.clientes.domain.model.Email;
import com.orbitapay.clientes.domain.model.MotivoBloqueio;
import com.orbitapay.clientes.domain.model.NomeCompleto;

@Component
public class ClienteRepositoryMongoAdapter implements ClienteRepository {

    private final ClienteMongoRepository mongo;

    public ClienteRepositoryMongoAdapter(ClienteMongoRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public String proximoId() {
        return new ObjectId().toHexString();
    }

    @Override
    public Optional<Cliente> buscarPorId(String clienteId) {
        return mongo.findById(clienteId).map(ClienteRepositoryMongoAdapter::paraDominio);
    }

    @Override
    public Optional<Cliente> buscarPorEmail(Email email) {
        return mongo.findByEmail(email.valor()).map(ClienteRepositoryMongoAdapter::paraDominio);
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
        return mongo.findAll(Sort.by("clienteDesde")).stream().map(ClienteRepositoryMongoAdapter::paraDominio).toList();
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
                cliente.pinCodificado(),
                cliente.bloqueado(),
                cliente.motivoBloqueio() == null ? null : cliente.motivoBloqueio().name(),
                cliente.tentativasFalhas(),
                cliente.clienteDesde());
    }

    private static Cliente paraDominio(ClienteDocument documento) {
        return Cliente.reconstituir(
                documento.id(),
                new NomeCompleto(documento.nome()),
                new Email(documento.email()),
                new Cpf(documento.cpf()),
                documento.pinCodificado(),
                documento.bloqueado(),
                documento.motivoBloqueio() == null ? null : MotivoBloqueio.valueOf(documento.motivoBloqueio()),
                documento.tentativasFalhas(),
                documento.clienteDesde());
    }
}
