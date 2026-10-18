package com.orbitapay.clientes.adapter.gateway.mongo.document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("contas")
public record ContaDocument(
        @Id String id,
        @Indexed(unique = true) String clienteId,
        String agencia,
        String numero,
        BigDecimal saldo,
        List<LancamentoDocument> lancamentos,
        Instant abertaEm) {
}
