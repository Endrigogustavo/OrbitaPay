package com.orbitapay.negociacao.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AtivoNegociavelDocumentRepository extends MongoRepository<AtivoNegociavelDocument, String> {
}
