package com.orbitapay.relatorios.persistence;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("fatos")
@CompoundIndex(name = "cliente_ocorrido", def = "{'clienteId': 1, 'ocorridoEm': 1}")
public record FatoDocument(
        @Id String eventoId,
        String tipo,
        String clienteId,
        String ticker,
        long quantidade,
        BigDecimal valor,
        String detalhe,
        @Indexed Instant ocorridoEm) {
}
