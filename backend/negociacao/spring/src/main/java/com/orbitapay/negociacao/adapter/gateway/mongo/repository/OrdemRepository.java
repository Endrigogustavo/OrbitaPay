package com.orbitapay.negociacao.adapter.gateway.mongo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.orbitapay.negociacao.adapter.gateway.mongo.document.OrdemDocument;

public interface OrdemRepository extends MongoRepository<OrdemDocument, String> {

    List<OrdemDocument> findByClienteIdOrderByCriadaEmDesc(String clienteId);
}
