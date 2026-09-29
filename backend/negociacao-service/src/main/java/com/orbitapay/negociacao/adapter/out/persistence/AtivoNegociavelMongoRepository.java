package com.orbitapay.negociacao.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AtivoNegociavelMongoRepository extends MongoRepository<AtivoNegociavelDocument, String> {
}
