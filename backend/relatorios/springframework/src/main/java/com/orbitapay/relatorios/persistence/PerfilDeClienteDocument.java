package com.orbitapay.relatorios.persistence;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("perfis_de_cliente")
public record PerfilDeClienteDocument(@Id String clienteId, String nome, boolean bloqueado, boolean ativo,
        Instant cadastradoEm) {
}
