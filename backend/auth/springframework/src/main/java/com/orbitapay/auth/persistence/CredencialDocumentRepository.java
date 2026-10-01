package com.orbitapay.auth.persistence;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CredencialDocumentRepository extends MongoRepository<CredencialDocument, String> {

    Optional<CredencialDocument> findByEmail(String email);

    boolean existsByEmailAndClienteIdNot(String email, String clienteId);
}
