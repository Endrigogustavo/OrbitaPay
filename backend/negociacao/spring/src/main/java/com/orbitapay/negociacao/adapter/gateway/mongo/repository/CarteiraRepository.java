package com.orbitapay.negociacao.adapter.gateway.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.orbitapay.negociacao.adapter.gateway.mongo.document.CarteiraDocument;

public interface CarteiraRepository extends MongoRepository<CarteiraDocument, String> {
}
