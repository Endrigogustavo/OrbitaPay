package com.orbitapay.auth.adapter.gateway.mongo.document;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("credenciais")
public record CredencialDocument(
        @Id String clienteId,
        @Indexed(unique = true) String email,
        String pinCodificado,
        int tentativasFalhas,
        Instant criadaEm) {
}
