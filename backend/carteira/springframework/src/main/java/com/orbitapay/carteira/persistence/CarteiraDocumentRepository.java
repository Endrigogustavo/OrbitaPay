package com.orbitapay.carteira.persistence;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CarteiraDocumentRepository extends MongoRepository<CarteiraDocument, String> {

    Optional<CarteiraDocument> findByClienteId(String clienteId);

    boolean existsByClienteId(String clienteId);

    void deleteByClienteId(String clienteId);
}
