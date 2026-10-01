package com.orbitapay.pagamentos.persistence;

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
        String provedor,
        String referenciaExterna,
        String codigo,
        String descricao,
        Instant validoAte,
        @Indexed String status,
        BigDecimal valorPago,
        Instant criadoEm,
        Instant concluidoEm) {
}
