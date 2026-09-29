package com.orbitapay.clientes.adapter.out.persistence;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClienteMongoRepository extends MongoRepository<ClienteDocument, String> {

    Optional<ClienteDocument> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, String id);
}
