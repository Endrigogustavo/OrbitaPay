package com.orbitapay.ativos.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AtivoDocumentRepository extends MongoRepository<AtivoDocument, String> {
}
