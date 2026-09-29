package com.orbitapay.ativos.adapter.out.messaging.mensagem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CotacoesAtualizadasMensagem(String eventoId, String evento, List<Cotacao> cotacoes, Instant enviadoEm) {

    public record Cotacao(String ticker, BigDecimal cotacao) {
    }
}
