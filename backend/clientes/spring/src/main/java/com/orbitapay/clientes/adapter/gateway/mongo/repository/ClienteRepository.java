package com.orbitapay.clientes.adapter.gateway.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.orbitapay.clientes.adapter.gateway.mongo.document.ClienteDocument;

public interface ClienteRepository extends MongoRepository<ClienteDocument, String> {

    boolean existsByEmailAndIdNot(String email, String id);
}
