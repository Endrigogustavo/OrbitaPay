package com.orbitapay.relatorios.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record ExtratoDeInvestimentos(
        String clienteId,
        LocalDate de,
        LocalDate ate,
        BigDecimal totalDepositado,
        BigDecimal totalComprado,
        BigDecimal totalVendido,
        long ordensExecutadas,
        long ordensRejeitadas,
        List<PorAtivo> porAtivo,
        List<Movimento> ultimosMovimentos,
        Instant geradoEm) {

    public record PorAtivo(String ticker, long quantidadeComprada, long quantidadeVendida, BigDecimal volumeComprado,
            BigDecimal volumeVendido) {
    }

    public record Movimento(String tipo, String ticker, long quantidade, BigDecimal valor, String detalhe,
            Instant ocorridoEm) {
    }
}
