package com.orbitapay.clientes.entity;

import java.time.Instant;

public record Cliente(String id, String nome, String email, String cpf, boolean bloqueado,
        MotivoBloqueio motivoBloqueio, Instant clienteDesde) {
}
