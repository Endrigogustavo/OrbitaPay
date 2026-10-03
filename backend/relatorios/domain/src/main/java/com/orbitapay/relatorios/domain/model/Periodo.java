package com.orbitapay.relatorios.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import com.orbitapay.relatorios.domain.exception.RegraDeNegocioException;

public record Periodo(LocalDate de, LocalDate ate, Instant inicio, Instant fim) {

    public static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    public static final int DIAS_PADRAO = 30;
    public static final int DIAS_MAXIMOS = 366;

    public static Periodo entre(LocalDate de, LocalDate ate, LocalDate hoje) {
        LocalDate fimDoPeriodo = ate == null ? hoje : ate;
        LocalDate inicioDoPeriodo = de == null ? fimDoPeriodo.minusDays(DIAS_PADRAO - 1) : de;
        if (inicioDoPeriodo.isAfter(fimDoPeriodo)) {
            throw new RegraDeNegocioException("A data inicial deve ser anterior à final");
        }
        if (ChronoUnit.DAYS.between(inicioDoPeriodo, fimDoPeriodo) >= DIAS_MAXIMOS) {
            throw new RegraDeNegocioException("O período máximo é de " + DIAS_MAXIMOS + " dias");
        }
        return new Periodo(inicioDoPeriodo, fimDoPeriodo, inicioDoPeriodo.atStartOfDay(FUSO).toInstant(),
                fimDoPeriodo.plusDays(1).atStartOfDay(FUSO).toInstant());
    }

    public LocalDate diaDe(Instant instante) {
        return instante.atZone(FUSO).toLocalDate();
    }
}
