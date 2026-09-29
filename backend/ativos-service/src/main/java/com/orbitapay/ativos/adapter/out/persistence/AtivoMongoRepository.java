package com.orbitapay.ativos.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AtivoMongoRepository extends MongoRepository<AtivoDocument, String> {
}
