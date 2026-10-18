package com.orbitapay.auth.adapter.gateway.mongo.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.orbitapay.auth.adapter.gateway.mongo.document.FuncionarioDocument;

public interface FuncionarioRepository extends MongoRepository<FuncionarioDocument, String> {

    Optional<FuncionarioDocument> findByEmail(String email);

    boolean existsByEmail(String email);
}
