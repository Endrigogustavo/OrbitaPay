package com.orbitapay.relatorios.application.usecase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.orbitapay.relatorios.application.dto.RelatorioGerencial;
import com.orbitapay.relatorios.domain.model.Fato;
import com.orbitapay.relatorios.domain.model.PerfilDeCliente;
import com.orbitapay.relatorios.domain.model.Periodo;
import com.orbitapay.relatorios.domain.model.TipoDeFato;
import com.orbitapay.relatorios.domain.repository.FatoRepository;
import com.orbitapay.relatorios.domain.repository.PerfilDeClienteRepository;

/** Visão do gerente: base de clientes, volume negociado, depósitos e rankings no período. */
public class GerarRelatorioGerencial {

    private static final int TAMANHO_DOS_RANKINGS = 5;

    private final FatoRepository fatos;
    private final PerfilDeClienteRepository perfis;
    private final Clock relogio;

    public GerarRelatorioGerencial(FatoRepository fatos, PerfilDeClienteRepository perfis, Clock relogio) {
        this.fatos = fatos;
        this.perfis = perfis;
        this.relogio = relogio;
    }

    public RelatorioGerencial executar(LocalDate de, LocalDate ate) {
        Instant agora = Instant.now(relogio);
        Periodo periodo = Periodo.entre(de, ate, LocalDate.ofInstant(agora, Periodo.FUSO));
        List<Fato> doPeriodo = fatos.listarNoPeriodo(periodo);
        List<PerfilDeCliente> base = perfis.listar();
        Map<String, PerfilDeCliente> porCliente = base.stream()
                .collect(Collectors.toMap(PerfilDeCliente::clienteId, Function.identity()));
        return new RelatorioGerencial(periodo.de(), periodo.ate(), clientes(base, doPeriodo), negociacao(doPeriodo),
                depositos(doPeriodo), ativosMaisNegociados(doPeriodo), maioresInvestidores(doPeriodo, porCliente),
                movimentoDiario(doPeriodo, periodo), agora);
    }

    private static RelatorioGerencial.Clientes clientes(List<PerfilDeCliente> base, List<Fato> fatos) {
        long ativos = base.stream().filter(PerfilDeCliente::ativo).count();
        long bloqueados = base.stream().filter(p -> p.ativo() && p.bloqueado()).count();
        return new RelatorioGerencial.Clientes(ativos, bloqueados, contar(fatos, TipoDeFato.CLIENTE_CADASTRADO),
                contar(fatos, TipoDeFato.CLIENTE_REMOVIDO), contar(fatos, TipoDeFato.CLIENTE_BLOQUEADO));
    }

    private static RelatorioGerencial.Negociacao negociacao(List<Fato> fatos) {
        long compras = contar(fatos, TipoDeFato.COMPRA_EXECUTADA);
        long vendas = contar(fatos, TipoDeFato.VENDA_EXECUTADA);
        long rejeitadas = contar(fatos, TipoDeFato.ORDEM_REJEITADA);
        long total = compras + vendas + rejeitadas;
        BigDecimal taxa = total == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(rejeitadas * 100).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_EVEN);
        return new RelatorioGerencial.Negociacao(compras, somar(fatos, TipoDeFato.COMPRA_EXECUTADA), vendas,
                somar(fatos, TipoDeFato.VENDA_EXECUTADA), rejeitadas, taxa);
    }

    private static RelatorioGerencial.Depositos depositos(List<Fato> fatos) {
        List<Fato> confirmados = fatos.stream().filter(f -> f.tipo() == TipoDeFato.DEPOSITO_CONFIRMADO).toList();
        BigDecimal volume = soma(confirmados);
        BigDecimal ticketMedio = confirmados.isEmpty() ? BigDecimal.ZERO
                : volume.divide(BigDecimal.valueOf(confirmados.size()), 2, RoundingMode.HALF_EVEN);
        List<RelatorioGerencial.PorMetodo> porMetodo = confirmados.stream()
                .collect(Collectors.groupingBy(f -> f.detalhe() == null ? "?" : f.detalhe()))
                .entrySet().stream()
                .map(e -> new RelatorioGerencial.PorMetodo(e.getKey(), e.getValue().size(), soma(e.getValue())))
                .sorted(Comparator.comparing(RelatorioGerencial.PorMetodo::volume).reversed())
                .toList();
        return new RelatorioGerencial.Depositos(confirmados.size(), volume, ticketMedio,
                contar(fatos, TipoDeFato.DEPOSITO_EXPIRADO), porMetodo);
    }

    private static List<RelatorioGerencial.AtivoNegociado> ativosMaisNegociados(List<Fato> fatos) {
        return fatos.stream().filter(f -> f.tipo().negociacao())
                .collect(Collectors.groupingBy(Fato::ticker))
                .entrySet().stream()
                .map(e -> new RelatorioGerencial.AtivoNegociado(e.getKey(), e.getValue().size(),
                        e.getValue().stream().mapToLong(Fato::quantidade).sum(), soma(e.getValue())))
                .sorted(Comparator.comparing(RelatorioGerencial.AtivoNegociado::volume).reversed())
                .limit(TAMANHO_DOS_RANKINGS)
                .toList();
    }

    private static List<RelatorioGerencial.Investidor> maioresInvestidores(List<Fato> fatos,
            Map<String, PerfilDeCliente> porCliente) {
        return fatos.stream().filter(f -> f.tipo().negociacao())
                .collect(Collectors.groupingBy(Fato::clienteId))
                .entrySet().stream()
                .map(e -> new RelatorioGerencial.Investidor(e.getKey(), nome(porCliente.get(e.getKey())),
                        e.getValue().size(), soma(e.getValue())))
                .sorted(Comparator.comparing(RelatorioGerencial.Investidor::volume).reversed())
                .limit(TAMANHO_DOS_RANKINGS)
                .toList();
    }

    private static List<RelatorioGerencial.Dia> movimentoDiario(List<Fato> fatos, Periodo periodo) {
        Map<LocalDate, List<Fato>> porDia = fatos.stream().collect(Collectors.groupingBy(f -> periodo.diaDe(f.ocorridoEm())));
        List<RelatorioGerencial.Dia> dias = new ArrayList<>();
        for (LocalDate dia = periodo.de(); !dia.isAfter(periodo.ate()); dia = dia.plusDays(1)) {
            List<Fato> doDia = porDia.getOrDefault(dia, List.of());
            dias.add(new RelatorioGerencial.Dia(dia, somar(doDia, TipoDeFato.COMPRA_EXECUTADA),
                    somar(doDia, TipoDeFato.VENDA_EXECUTADA), somar(doDia, TipoDeFato.DEPOSITO_CONFIRMADO)));
        }
        return dias;
    }

    private static String nome(PerfilDeCliente perfil) {
        return perfil == null || perfil.nome() == null ? "Cliente encerrado" : perfil.nome();
    }

    private static long contar(List<Fato> fatos, TipoDeFato tipo) {
        return fatos.stream().filter(doTipo(tipo)).count();
    }

    private static BigDecimal somar(List<Fato> fatos, TipoDeFato tipo) {
        return soma(fatos.stream().filter(doTipo(tipo)).toList());
    }

    private static BigDecimal soma(List<Fato> fatos) {
        return fatos.stream().map(Fato::valor).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_EVEN);
    }

    private static Predicate<Fato> doTipo(TipoDeFato tipo) {
        return fato -> fato.tipo() == tipo;
    }
}
