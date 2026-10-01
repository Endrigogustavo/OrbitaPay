package com.orbitapay.clientes.persistence;

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
        boolean bloqueado,
        String motivoBloqueio,
        Instant clienteDesde) {
}
