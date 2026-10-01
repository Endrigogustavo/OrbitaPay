package com.orbitapay.ativos.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface BolsaDocumentRepository extends MongoRepository<BolsaDocument, String> {
}
