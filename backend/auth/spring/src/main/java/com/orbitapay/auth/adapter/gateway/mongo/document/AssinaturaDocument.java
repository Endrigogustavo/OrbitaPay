package com.orbitapay.auth.adapter.gateway.mongo.document;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("assinaturas")
public record AssinaturaDocument(
        @Id String tokenHash,
        String clienteId,
        @Indexed(expireAfter = "0s") Instant expiraEm) {
}
