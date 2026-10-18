package com.orbitapay.ativos.adapter.gateway.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.orbitapay.ativos.adapter.gateway.mongo.document.BolsaDocument;

public interface BolsaRepository extends MongoRepository<BolsaDocument, String> {
}
