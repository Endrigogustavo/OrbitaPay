package com.orbitapay.auth.adapter.gateway.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.orbitapay.auth.adapter.gateway.mongo.document.SessaoDocument;

public interface SessaoRepository extends MongoRepository<SessaoDocument, String> {

    void deleteBySujeito(String sujeito);
}
