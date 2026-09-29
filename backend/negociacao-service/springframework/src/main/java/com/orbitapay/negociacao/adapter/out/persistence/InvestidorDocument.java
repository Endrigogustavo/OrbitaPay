package com.orbitapay.negociacao.adapter.out.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("investidores")
public record InvestidorDocument(@Id String clienteId, boolean bloqueado) {
}
