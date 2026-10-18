package com.orbitapay.negociacao.adapter.gateway.mongo.document;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("carteiras")
public record CarteiraDocument(@Id String clienteId, List<PosicaoDocument> posicoes) {
}
