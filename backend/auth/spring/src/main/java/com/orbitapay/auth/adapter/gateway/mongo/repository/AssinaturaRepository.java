package com.orbitapay.auth.adapter.gateway.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.orbitapay.auth.adapter.gateway.mongo.document.AssinaturaDocument;

public interface AssinaturaRepository extends MongoRepository<AssinaturaDocument, String> {
}
