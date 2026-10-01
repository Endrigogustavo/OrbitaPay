package com.orbitapay.relatorios.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record RelatorioGerencial(
        LocalDate de,
        LocalDate ate,
        Clientes clientes,
        Negociacao negociacao,
        Depositos depositos,
        List<AtivoNegociado> ativosMaisNegociados,
        List<Investidor> maioresInvestidores,
        List<Dia> movimentoDiario,
        Instant geradoEm) {

    public record Clientes(long ativos, long bloqueados, long novosNoPeriodo, long encerradosNoPeriodo,
            long bloqueiosNoPeriodo) {
    }

    public record Negociacao(long compras, BigDecimal volumeComprado, long vendas, BigDecimal volumeVendido,
            long rejeitadas, BigDecimal taxaDeRejeicaoPercentual) {
    }

    public record Depositos(long confirmados, BigDecimal volume, BigDecimal ticketMedio, long expirados,
            List<PorMetodo> porMetodo) {
    }

    public record PorMetodo(String metodo, long quantidade, BigDecimal volume) {
    }

    public record AtivoNegociado(String ticker, long ordens, long quantidade, BigDecimal volume) {
    }

    public record Investidor(String clienteId, String nome, long ordens, BigDecimal volume) {
    }

    public record Dia(LocalDate dia, BigDecimal compras, BigDecimal vendas, BigDecimal depositos) {
    }
}
