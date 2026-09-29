package com.orbitapay.ativos.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface BolsaMongoRepository extends MongoRepository<BolsaDocument, String> {
}
