package com.orbitapay.negociacao.adapter.out.persistence;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrdemMongoRepository extends MongoRepository<OrdemDocument, String> {

    List<OrdemDocument> findByClienteIdOrderByCriadaEmDesc(String clienteId);
}
