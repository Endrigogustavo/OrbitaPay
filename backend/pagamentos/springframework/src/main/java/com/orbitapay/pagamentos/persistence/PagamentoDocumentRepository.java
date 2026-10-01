package com.orbitapay.pagamentos.persistence;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PagamentoDocumentRepository extends MongoRepository<PagamentoDocument, String> {

    List<PagamentoDocument> findByClienteIdOrderByCriadoEmDesc(String clienteId);

    List<PagamentoDocument> findByStatusOrderByCriadoEmAsc(String status, Pageable pagina);
}
