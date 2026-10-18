package com.orbitapay.pagamentos.adapter.gateway.mongo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.orbitapay.pagamentos.adapter.gateway.mongo.document.PagamentoDocument;

public interface PagamentoRepository extends MongoRepository<PagamentoDocument, String> {

    List<PagamentoDocument> findByClienteIdOrderByCriadoEmDesc(String clienteId);

    List<PagamentoDocument> findByStatus(String status);

    void deleteByClienteId(String clienteId);
}
