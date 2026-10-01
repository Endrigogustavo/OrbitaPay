package com.orbitapay.pagamentos.application.dto;

import java.time.Instant;

import com.orbitapay.pagamentos.domain.model.Dinheiro;

/** Situação de uma cobrança já traduzida para o vocabulário do OrbitaPay, qualquer que seja o provedor. */
public record SituacaoDaCobranca(Estado estado, Dinheiro valorPago, Instant pagaEm) {

    public enum Estado {
        AGUARDANDO,
        PAGA,
        EXPIRADA
    }

    public static SituacaoDaCobranca aguardando() {
        return new SituacaoDaCobranca(Estado.AGUARDANDO, null, null);
    }

    public static SituacaoDaCobranca paga(Dinheiro valorPago, Instant pagaEm) {
        return new SituacaoDaCobranca(Estado.PAGA, valorPago, pagaEm);
    }

    public static SituacaoDaCobranca expirada() {
        return new SituacaoDaCobranca(Estado.EXPIRADA, null, null);
    }
}
