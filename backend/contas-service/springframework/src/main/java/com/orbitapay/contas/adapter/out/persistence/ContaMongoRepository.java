package com.orbitapay.contas.adapter.out.persistence;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ContaMongoRepository extends MongoRepository<ContaDocument, String> {

    Optional<ContaDocument> findByClienteId(String clienteId);

    boolean existsByClienteId(String clienteId);

    boolean existsByNumero(String numero);

    void deleteByClienteId(String clienteId);
}
