package com.orbitapay.auth.adapter.gateway.mongo.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.orbitapay.auth.adapter.gateway.mongo.document.CredencialDocument;

public interface CredencialRepository extends MongoRepository<CredencialDocument, String> {

    Optional<CredencialDocument> findByEmail(String email);

    boolean existsByEmailAndClienteIdNot(String email, String clienteId);
}
