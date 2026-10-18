package com.orbitapay.clientes.adapter.gateway.mongo.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.orbitapay.clientes.adapter.gateway.mongo.document.ContaDocument;

public interface ContaRepository extends MongoRepository<ContaDocument, String> {

    Optional<ContaDocument> findByClienteId(String clienteId);

    void deleteByClienteId(String clienteId);
}
