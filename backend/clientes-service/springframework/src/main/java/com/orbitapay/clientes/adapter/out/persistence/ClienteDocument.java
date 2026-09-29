package com.orbitapay.clientes.adapter.out.persistence;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("clientes")
public record ClienteDocument(
        @Id String id,
        String nome,
        @Indexed(unique = true) String email,
        String cpf,
        String pinCodificado,
        boolean bloqueado,
        String motivoBloqueio,
        int tentativasFalhas,
        Instant clienteDesde) {
}
