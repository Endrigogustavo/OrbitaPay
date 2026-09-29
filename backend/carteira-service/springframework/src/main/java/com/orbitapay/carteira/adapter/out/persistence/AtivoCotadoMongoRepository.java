package com.orbitapay.carteira.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AtivoCotadoMongoRepository extends MongoRepository<AtivoCotadoDocument, String> {
}
