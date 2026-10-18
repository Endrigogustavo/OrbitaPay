package com.orbitapay.pagamentos.adapter.gateway.mongo.document;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("pagamentos")
public record PagamentoDocument(
        @Id String id,
        @Indexed String clienteId,
        BigDecimal valor,
        String metodo,
        String codigoDaCobranca,
        String descricaoDaCobranca,
        Instant cobrancaValidaAte,
        @Indexed String status,
        BigDecimal valorPago,
        Instant criadoEm,
        Instant concluidoEm) {
}
