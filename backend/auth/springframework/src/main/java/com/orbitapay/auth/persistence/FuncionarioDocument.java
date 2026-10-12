package com.orbitapay.auth.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("funcionarios")
public record FuncionarioDocument(
        @Id String id,
        String nome,
        @Indexed(unique = true) String email,
        String pinCodificado,
        String perfil) {
}
