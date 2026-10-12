package com.orbitapay.auth.persistence;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface FuncionarioDocumentRepository extends MongoRepository<FuncionarioDocument, String> {

    Optional<FuncionarioDocument> findByEmail(String email);

    boolean existsByEmail(String email);
}
