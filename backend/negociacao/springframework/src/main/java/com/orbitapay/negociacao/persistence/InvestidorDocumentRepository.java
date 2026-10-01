package com.orbitapay.negociacao.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface InvestidorDocumentRepository extends MongoRepository<InvestidorDocument, String> {
}
