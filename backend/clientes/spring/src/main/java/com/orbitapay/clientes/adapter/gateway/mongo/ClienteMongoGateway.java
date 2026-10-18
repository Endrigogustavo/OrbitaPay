package com.orbitapay.clientes.adapter.gateway.mongo;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.orbitapay.clientes.entity.Cliente;
import com.orbitapay.clientes.entity.MotivoBloqueio;
import com.orbitapay.clientes.application.gateway.IClienteGateway;
import com.orbitapay.clientes.adapter.gateway.mongo.document.ClienteDocument;
import com.orbitapay.clientes.adapter.gateway.mongo.repository.ClienteRepository;

@Component
public class ClienteMongoGateway implements IClienteGateway {

    private final ClienteRepository clienteRepository;

    public ClienteMongoGateway(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public void salvar(Cliente cliente) {
        clienteRepository.save(new ClienteDocument(cliente.id(), cliente.nome(), cliente.email(), cliente.cpf(),
                cliente.bloqueado(), cliente.motivoBloqueio() == null ? null : cliente.motivoBloqueio().name(),
                cliente.clienteDesde()));
    }

    @Override
    public Optional<Cliente> buscarPorId(String clienteId) {
        return clienteRepository.findById(clienteId).map(ClienteMongoGateway::converterDocumentoParaEntidade);
    }

    @Override
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll().stream().map(ClienteMongoGateway::converterDocumentoParaEntidade).toList();
    }

    @Override
    public boolean emailEmUsoPorOutroCliente(String email, String clienteId) {
        return clienteRepository.existsByEmailAndIdNot(email, clienteId);
    }

    @Override
    public void remover(String clienteId) {
        clienteRepository.deleteById(clienteId);
    }

    private static Cliente converterDocumentoParaEntidade(ClienteDocument documento) {
        MotivoBloqueio motivo = documento.motivoBloqueio() == null ? null
                : MotivoBloqueio.valueOf(documento.motivoBloqueio());
        return new Cliente(documento.id(), documento.nome(), documento.email(), documento.cpf(), documento.bloqueado(),
                motivo, documento.clienteDesde());
    }
}
