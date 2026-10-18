package com.orbitapay.ativos.adapter.gateway.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.orbitapay.ativos.adapter.gateway.mongo.document.AtivoDocument;

public interface AtivoRepository extends MongoRepository<AtivoDocument, String> {
}
