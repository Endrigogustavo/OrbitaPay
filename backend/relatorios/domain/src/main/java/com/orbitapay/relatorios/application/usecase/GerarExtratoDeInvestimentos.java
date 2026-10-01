package com.orbitapay.relatorios.application.usecase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import com.orbitapay.relatorios.application.dto.ExtratoDeInvestimentos;
import com.orbitapay.relatorios.domain.model.Fato;
import com.orbitapay.relatorios.domain.model.Periodo;
import com.orbitapay.relatorios.domain.model.TipoDeFato;
import com.orbitapay.relatorios.domain.repository.FatoRepository;

/** Visão do cliente: quanto depositou, comprou e vendeu no período, por ativo, e os últimos movimentos. */
public class GerarExtratoDeInvestimentos {

    private static final int ULTIMOS_MOVIMENTOS = 20;

    private final FatoRepository fatos;
    private final Clock relogio;

    public GerarExtratoDeInvestimentos(FatoRepository fatos, Clock relogio) {
        this.fatos = fatos;
        this.relogio = relogio;
    }

    public ExtratoDeInvestimentos executar(String clienteId, LocalDate de, LocalDate ate) {
        Instant agora = Instant.now(relogio);
        Periodo periodo = Periodo.entre(de, ate, LocalDate.ofInstant(agora, Periodo.FUSO));
        List<Fato> doCliente = fatos.listarDoClienteNoPeriodo(clienteId, periodo);
        List<ExtratoDeInvestimentos.PorAtivo> porAtivo = doCliente.stream()
                .filter(f -> f.tipo().negociacao())
                .map(Fato::ticker).distinct().sorted()
                .map(ticker -> porAtivo(ticker, doCliente))
                .toList();
        List<ExtratoDeInvestimentos.Movimento> movimentos = doCliente.stream()
                .filter(f -> f.tipo().negociacao() || f.tipo() == TipoDeFato.ORDEM_REJEITADA
                        || f.tipo() == TipoDeFato.DEPOSITO_CONFIRMADO)
                .sorted(Comparator.comparing(Fato::ocorridoEm).reversed())
                .limit(ULTIMOS_MOVIMENTOS)
                .map(f -> new ExtratoDeInvestimentos.Movimento(f.tipo().name(), f.ticker(), f.quantidade(), f.valor(),
                        f.detalhe(), f.ocorridoEm()))
                .toList();
        return new ExtratoDeInvestimentos(clienteId, periodo.de(), periodo.ate(),
                somar(doCliente, TipoDeFato.DEPOSITO_CONFIRMADO, null),
                somar(doCliente, TipoDeFato.COMPRA_EXECUTADA, null), somar(doCliente, TipoDeFato.VENDA_EXECUTADA, null),
                doCliente.stream().filter(f -> f.tipo().negociacao()).count(),
                doCliente.stream().filter(f -> f.tipo() == TipoDeFato.ORDEM_REJEITADA).count(),
                porAtivo, movimentos, agora);
    }

    private static ExtratoDeInvestimentos.PorAtivo porAtivo(String ticker, List<Fato> fatos) {
        return new ExtratoDeInvestimentos.PorAtivo(ticker, quantidade(fatos, TipoDeFato.COMPRA_EXECUTADA, ticker),
                quantidade(fatos, TipoDeFato.VENDA_EXECUTADA, ticker), somar(fatos, TipoDeFato.COMPRA_EXECUTADA, ticker),
                somar(fatos, TipoDeFato.VENDA_EXECUTADA, ticker));
    }

    private static long quantidade(List<Fato> fatos, TipoDeFato tipo, String ticker) {
        return fatos.stream().filter(f -> f.tipo() == tipo && ticker.equals(f.ticker())).mapToLong(Fato::quantidade).sum();
    }

    private static BigDecimal somar(List<Fato> fatos, TipoDeFato tipo, String ticker) {
        return fatos.stream()
                .filter(f -> f.tipo() == tipo && (ticker == null || ticker.equals(f.ticker())))
                .map(Fato::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_EVEN);
    }
}
