package com.orbitapay.relatorios.persistence;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface FatoDocumentRepository extends MongoRepository<FatoDocument, String> {

    @Query("{ 'ocorridoEm': { $gte: ?0, $lt: ?1 } }")
    List<FatoDocument> buscarNoPeriodo(Instant inicio, Instant fim);

    @Query("{ 'clienteId': ?0, 'ocorridoEm': { $gte: ?1, $lt: ?2 } }")
    List<FatoDocument> buscarDoClienteNoPeriodo(String clienteId, Instant inicio, Instant fim);
}
