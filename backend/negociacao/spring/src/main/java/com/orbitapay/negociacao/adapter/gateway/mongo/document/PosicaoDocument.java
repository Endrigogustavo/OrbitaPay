package com.orbitapay.negociacao.adapter.gateway.mongo.document;

import java.math.BigDecimal;

public record PosicaoDocument(String ticker, long quantidade, BigDecimal precoMedio) {
}
