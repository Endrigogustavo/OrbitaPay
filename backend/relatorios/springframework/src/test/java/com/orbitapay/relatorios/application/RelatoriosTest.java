package com.orbitapay.relatorios.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.orbitapay.relatorios.application.dto.ExtratoDeInvestimentos;
import com.orbitapay.relatorios.application.dto.RelatorioGerencial;
import com.orbitapay.relatorios.application.usecase.GerarExtratoDeInvestimentos;
import com.orbitapay.relatorios.application.usecase.GerarRelatorioGerencial;
import com.orbitapay.relatorios.application.usecase.RegistrarFato;
import com.orbitapay.relatorios.domain.model.Fato;
import com.orbitapay.relatorios.domain.model.PerfilDeCliente;
import com.orbitapay.relatorios.domain.model.Periodo;
import com.orbitapay.relatorios.domain.model.TipoDeFato;
import com.orbitapay.relatorios.domain.repository.FatoRepository;
import com.orbitapay.relatorios.domain.repository.PerfilDeClienteRepository;

class RelatoriosTest {

    private static final Instant HOJE = Instant.parse("2026-09-30T15:00:00Z");

    private final FatosEmMemoria fatos = new FatosEmMemoria();
    private final PerfisEmMemoria perfis = new PerfisEmMemoria();
    private final Clock relogio = Clock.fixed(HOJE, ZoneOffset.UTC);
    private final RegistrarFato registrar = new RegistrarFato(fatos, perfis);

    @Test
    void mesmoEventoEntregueDuasVezesContaUmaSo() {
        Fato compra = fato("e1", TipoDeFato.COMPRA_EXECUTADA, "ana", "PETR4", 10, "382.00");
        registrar.executar(compra, null);
        registrar.executar(compra, null);

        RelatorioGerencial relatorio = new GerarRelatorioGerencial(fatos, perfis, relogio).executar(null, null);

        assertEquals(1, relatorio.negociacao().compras());
        assertEquals(new BigDecimal("382.00"), relatorio.negociacao().volumeComprado());
    }

    @Test
    void relatorioGerencialConsolidaClientesNegociacaoEDepositos() {
        registrar.executar(fato("c1", TipoDeFato.CLIENTE_CADASTRADO, "ana", null, 0, "0"), "Ana Ribeiro");
        registrar.executar(fato("c2", TipoDeFato.CLIENTE_CADASTRADO, "bruno", null, 0, "0"), "Bruno Tavares");
        registrar.executar(fato("c3", TipoDeFato.CLIENTE_BLOQUEADO, "bruno", null, 0, "0"), null);
        registrar.executar(fato("o1", TipoDeFato.COMPRA_EXECUTADA, "ana", "PETR4", 10, "382.00"), null);
        registrar.executar(fato("o2", TipoDeFato.VENDA_EXECUTADA, "ana", "PETR4", 5, "200.00"), null);
        registrar.executar(fato("o3", TipoDeFato.COMPRA_EXECUTADA, "bruno", "AAPL", 1, "1222.48"), null);
        registrar.executar(fato("o4", TipoDeFato.ORDEM_REJEITADA, "bruno", "ORBT3", 1, "10.00"), null);
        registrar.executar(new Fato("p1", TipoDeFato.DEPOSITO_CONFIRMADO, "ana", null, 0, new BigDecimal("100.00"),
                "PIX", HOJE), null);
        registrar.executar(new Fato("p2", TipoDeFato.DEPOSITO_CONFIRMADO, "bruno", null, 0, new BigDecimal("300.00"),
                "BOLETO", HOJE), null);

        RelatorioGerencial relatorio = new GerarRelatorioGerencial(fatos, perfis, relogio).executar(null, null);

        assertEquals(2, relatorio.clientes().ativos());
        assertEquals(1, relatorio.clientes().bloqueados());
        assertEquals(2, relatorio.negociacao().compras());
        assertEquals(new BigDecimal("1604.48"), relatorio.negociacao().volumeComprado());
        assertEquals(new BigDecimal("25.00"), relatorio.negociacao().taxaDeRejeicaoPercentual());
        assertEquals(new BigDecimal("400.00"), relatorio.depositos().volume());
        assertEquals(new BigDecimal("200.00"), relatorio.depositos().ticketMedio());
        assertEquals("AAPL", relatorio.ativosMaisNegociados().getFirst().ticker());
        assertEquals("Bruno Tavares", relatorio.maioresInvestidores().getFirst().nome());
        assertEquals(Periodo.DIAS_PADRAO, relatorio.movimentoDiario().size());
    }

    @Test
    void extratoDoClienteSomaPorAtivo() {
        registrar.executar(fato("o1", TipoDeFato.COMPRA_EXECUTADA, "ana", "PETR4", 10, "382.00"), null);
        registrar.executar(fato("o2", TipoDeFato.VENDA_EXECUTADA, "ana", "PETR4", 4, "160.00"), null);
        registrar.executar(fato("o3", TipoDeFato.COMPRA_EXECUTADA, "bruno", "PETR4", 1, "38.20"), null);

        ExtratoDeInvestimentos extrato = new GerarExtratoDeInvestimentos(fatos, relogio)
                .executar("ana", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));

        assertEquals(new BigDecimal("382.00"), extrato.totalComprado());
        assertEquals(new BigDecimal("160.00"), extrato.totalVendido());
        assertEquals(1, extrato.porAtivo().size());
        assertEquals(6, extrato.porAtivo().getFirst().quantidadeComprada() - extrato.porAtivo().getFirst().quantidadeVendida());
        assertEquals(2, extrato.ultimosMovimentos().size());
    }

    private static Fato fato(String eventoId, TipoDeFato tipo, String clienteId, String ticker, long quantidade,
            String valor) {
        return new Fato(eventoId, tipo, clienteId, ticker, quantidade, new BigDecimal(valor), null, HOJE);
    }

    private static final class FatosEmMemoria implements FatoRepository {

        private final Map<String, Fato> porEvento = new LinkedHashMap<>();

        @Override
        public boolean registrar(Fato fato) {
            return porEvento.putIfAbsent(fato.eventoId(), fato) == null;
        }

        @Override
        public List<Fato> listarNoPeriodo(Periodo periodo) {
            return porEvento.values().stream()
                    .filter(f -> !f.ocorridoEm().isBefore(periodo.inicio()) && f.ocorridoEm().isBefore(periodo.fim()))
                    .toList();
        }

        @Override
        public List<Fato> listarDoClienteNoPeriodo(String clienteId, Periodo periodo) {
            return listarNoPeriodo(periodo).stream().filter(f -> f.clienteId().equals(clienteId)).toList();
        }
    }

    private static final class PerfisEmMemoria implements PerfilDeClienteRepository {

        private final Map<String, PerfilDeCliente> porCliente = new LinkedHashMap<>();

        @Override
        public Optional<PerfilDeCliente> buscar(String clienteId) {
            return Optional.ofNullable(porCliente.get(clienteId));
        }

        @Override
        public List<PerfilDeCliente> listar() {
            return new ArrayList<>(porCliente.values());
        }

        @Override
        public void salvar(PerfilDeCliente perfil) {
            porCliente.put(perfil.clienteId(), perfil);
        }
    }
}
