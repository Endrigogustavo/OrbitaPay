package com.orbitapay.ativos.adapter.out.messaging.mensagem;

import java.math.BigDecimal;
import java.time.Instant;

public record AtivoMensagem(
        String eventoId,
        String evento,
        String ticker,
        String nome,
        String setor,
        String bolsa,
        String moeda,
        BigDecimal cambio,
        BigDecimal cotacao,
        long quantidadeEmitida,
        String mensagem,
        Instant enviadoEm) {
}
