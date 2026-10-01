package com.orbitapay.clientes.persistence;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClienteDocumentRepository extends MongoRepository<ClienteDocument, String> {

    Optional<ClienteDocument> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, String id);
}
