package com.orbitapay.ativos.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CotacoesAtualizadas(List<Cotacao> cotacoes, Instant ocorridoEm) implements EventoDeAtivo {

    public CotacoesAtualizadas {
        cotacoes = List.copyOf(cotacoes);
    }

    public record Cotacao(String ticker, BigDecimal valor) {
    }
}
