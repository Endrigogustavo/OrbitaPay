package com.orbitapay.carteira.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AtivoCotadoDocumentRepository extends MongoRepository<AtivoCotadoDocument, String> {
}
