package com.orbitapay.auth.adapter.gateway.mongo.document;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("sessoes")
public record SessaoDocument(
        @Id String tokenHash,
        String perfil,
        @Indexed String sujeito,
        String clienteId,
        String nome,
        @Indexed(expireAfter = "0s") Instant expiraEm) {
}
