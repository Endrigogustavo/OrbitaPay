package com.orbitapay.negociacao.persistence;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrdemDocumentRepository extends MongoRepository<OrdemDocument, String> {

    List<OrdemDocument> findByClienteIdOrderByCriadaEmDesc(String clienteId);
}
