package com.orbitapay.relatorios.application.usecase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import com.orbitapay.relatorios.application.dto.ExtratoDeInvestimentos;
import com.orbitapay.relatorios.domain.model.Fato;
import com.orbitapay.relatorios.domain.model.Periodo;
import com.orbitapay.relatorios.domain.model.TipoDeFato;
import com.orbitapay.relatorios.domain.repository.FatoRepository;

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

        TreeSet<String> tickers = new TreeSet<>();
        long ordensExecutadas = 0;
        long ordensRejeitadas = 0;
        for (Fato fato : doCliente) {
            if (fato.tipo().negociacao()) {
                tickers.add(fato.ticker());
                ordensExecutadas++;
            }
            if (fato.tipo() == TipoDeFato.ORDEM_REJEITADA) {
                ordensRejeitadas++;
            }
        }

        List<ExtratoDeInvestimentos.PorAtivo> porAtivo = new ArrayList<>();
        for (String ticker : tickers) {
            porAtivo.add(new ExtratoDeInvestimentos.PorAtivo(ticker,
                    quantidade(doCliente, TipoDeFato.COMPRA_EXECUTADA, ticker),
                    quantidade(doCliente, TipoDeFato.VENDA_EXECUTADA, ticker),
                    somar(doCliente, TipoDeFato.COMPRA_EXECUTADA, ticker),
                    somar(doCliente, TipoDeFato.VENDA_EXECUTADA, ticker)));
        }

        return new ExtratoDeInvestimentos(clienteId, periodo.de(), periodo.ate(),
                somar(doCliente, TipoDeFato.DEPOSITO_CONFIRMADO, null),
                somar(doCliente, TipoDeFato.COMPRA_EXECUTADA, null),
                somar(doCliente, TipoDeFato.VENDA_EXECUTADA, null),
                ordensExecutadas, ordensRejeitadas, porAtivo, ultimosMovimentos(doCliente), agora);
    }

    private static List<ExtratoDeInvestimentos.Movimento> ultimosMovimentos(List<Fato> fatos) {
        List<Fato> relevantes = new ArrayList<>();
        for (Fato fato : fatos) {
            if (fato.tipo().negociacao() || fato.tipo() == TipoDeFato.ORDEM_REJEITADA
                    || fato.tipo() == TipoDeFato.DEPOSITO_CONFIRMADO) {
                relevantes.add(fato);
            }
        }
        relevantes.sort((a, b) -> b.ocorridoEm().compareTo(a.ocorridoEm()));

        List<ExtratoDeInvestimentos.Movimento> movimentos = new ArrayList<>();
        for (Fato fato : relevantes.subList(0, Math.min(ULTIMOS_MOVIMENTOS, relevantes.size()))) {
            movimentos.add(new ExtratoDeInvestimentos.Movimento(fato.tipo().name(), fato.ticker(), fato.quantidade(),
                    fato.valor(), fato.detalhe(), fato.ocorridoEm()));
        }
        return movimentos;
    }

    private static long quantidade(List<Fato> fatos, TipoDeFato tipo, String ticker) {
        long total = 0;
        for (Fato fato : fatos) {
            if (fato.tipo() == tipo && ticker.equals(fato.ticker())) {
                total += fato.quantidade();
            }
        }
        return total;
    }

    private static BigDecimal somar(List<Fato> fatos, TipoDeFato tipo, String ticker) {
        BigDecimal total = BigDecimal.ZERO;
        for (Fato fato : fatos) {
            if (fato.tipo() == tipo && (ticker == null || ticker.equals(fato.ticker()))) {
                total = total.add(fato.valor());
            }
        }
        return total.setScale(2, RoundingMode.HALF_EVEN);
    }
}
