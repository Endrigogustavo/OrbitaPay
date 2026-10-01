package com.orbitapay.pagamentos.acl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.orbitapay.pagamentos.acl.boleto.AdaptadorBoleto;
import com.orbitapay.pagamentos.acl.pix.AdaptadorPix;
import com.orbitapay.pagamentos.acl.ted.AdaptadorTed;
import com.orbitapay.pagamentos.application.dto.CobrancaEmitida;
import com.orbitapay.pagamentos.application.dto.SituacaoDaCobranca;
import com.orbitapay.pagamentos.application.dto.SolicitacaoDeCobranca;
import com.orbitapay.pagamentos.domain.model.Dinheiro;
import com.orbitapay.pagamentos.domain.model.MetodoDePagamento;
import com.orbitapay.pagamentos.provedor.ProvedoresSimuladosProperties;
import com.orbitapay.pagamentos.provedor.boleto.BancoEmissorSimulado;
import com.orbitapay.pagamentos.provedor.pix.PspPixSimulado;
import com.orbitapay.pagamentos.provedor.ted.SpbSimulado;

class AdaptadoresDeProvedorTest {

    private final RelogioAjustavel relogio = new RelogioAjustavel(Instant.parse("2026-09-30T13:00:00Z"));
    private final FachadaDeProvedores fachada = fachada();

    @Test
    void pixFicaAguardandoAteALiquidacaoEDepoisVoltaPagoComValorEHorarioTraduzidos() {
        CobrancaEmitida cobranca = emitir("1234.56", MetodoDePagamento.PIX);
        assertTrue(cobranca.instrucoes().codigo().startsWith("000201"), "Pix copia e cola em formato BR Code");
        assertEquals(SituacaoDaCobranca.Estado.AGUARDANDO, consultar(MetodoDePagamento.PIX, cobranca).estado());

        relogio.avancar(Duration.ofSeconds(3));
        SituacaoDaCobranca paga = consultar(MetodoDePagamento.PIX, cobranca);

        assertEquals(SituacaoDaCobranca.Estado.PAGA, paga.estado());
        assertEquals(Dinheiro.de("1234.56"), paga.valorPago());
        assertEquals(Instant.parse("2026-09-30T13:00:03Z"), paga.pagaEm());
    }

    @Test
    void boletoTraduzCentavosEHorarioDeBrasiliaSemFuso() {
        CobrancaEmitida cobranca = emitir("250.00", MetodoDePagamento.BOLETO);
        assertTrue(cobranca.instrucoes().descricao().startsWith("Linha digitável · vence em 03/10/2026"));

        relogio.avancar(Duration.ofSeconds(10));
        SituacaoDaCobranca pago = consultar(MetodoDePagamento.BOLETO, cobranca);

        assertEquals(SituacaoDaCobranca.Estado.PAGA, pago.estado());
        assertEquals(Dinheiro.de("250.00"), pago.valorPago());
        assertEquals(Instant.parse("2026-09-30T13:00:10Z"), pago.pagaEm());
    }

    @Test
    void tedTraduzValorNoFormatoBrasileiro() {
        CobrancaEmitida cobranca = emitir("12480.55", MetodoDePagamento.TED);
        assertTrue(cobranca.instrucoes().codigo().contains("Ag 0001"));

        relogio.avancar(Duration.ofSeconds(6));
        SituacaoDaCobranca creditada = consultar(MetodoDePagamento.TED, cobranca);

        assertEquals(SituacaoDaCobranca.Estado.PAGA, creditada.estado());
        assertEquals(Dinheiro.de("12480.55"), creditada.valorPago());
    }

    @Test
    void cobrancaNaoPagaExpiraDepoisDaValidade() {
        CobrancaEmitida cobranca = emitir("100.99", MetodoDePagamento.PIX);
        relogio.avancar(Duration.ofMinutes(10));
        assertEquals(SituacaoDaCobranca.Estado.AGUARDANDO, consultar(MetodoDePagamento.PIX, cobranca).estado());

        relogio.avancar(Duration.ofMinutes(25));
        assertEquals(SituacaoDaCobranca.Estado.EXPIRADA, consultar(MetodoDePagamento.PIX, cobranca).estado());
    }

    private CobrancaEmitida emitir(String valor, MetodoDePagamento metodo) {
        return fachada.emitir(new SolicitacaoDeCobranca("pagamento-1", "cliente-1", Dinheiro.de(valor), metodo));
    }

    private SituacaoDaCobranca consultar(MetodoDePagamento metodo, CobrancaEmitida cobranca) {
        return fachada.consultar(metodo, cobranca.referenciaExterna());
    }

    private FachadaDeProvedores fachada() {
        ProvedoresSimuladosProperties simulados = new ProvedoresSimuladosProperties(
                new ProvedoresSimuladosProperties.Pix("99999999", 3),
                new ProvedoresSimuladosProperties.Boleto("999", 10),
                new ProvedoresSimuladosProperties.Ted("999", "0001", "99999-9", "OrbitaPay S.A.", 6));
        AclProperties acl = new AclProperties(new AclProperties.Pix("pix@orbitapay.com.br", 1800),
                new AclProperties.Boleto(3), new AclProperties.Ted(24));
        return new FachadaDeProvedores(List.of(
                new AdaptadorPix(new PspPixSimulado(simulados, relogio), acl),
                new AdaptadorBoleto(new BancoEmissorSimulado(simulados, relogio), acl, relogio),
                new AdaptadorTed(new SpbSimulado(simulados, relogio), acl)));
    }

    private static final class RelogioAjustavel extends Clock {

        private Instant agora;

        RelogioAjustavel(Instant inicio) {
            this.agora = inicio;
        }

        void avancar(Duration duracao) {
            agora = agora.plus(duracao);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zona) {
            RelogioAjustavel relogio = this;
            return new Clock() {
                @Override
                public ZoneId getZone() {
                    return zona;
                }

                @Override
                public Clock withZone(ZoneId outra) {
                    return relogio.withZone(outra);
                }

                @Override
                public Instant instant() {
                    return relogio.instant();
                }
            };
        }

        @Override
        public Instant instant() {
            return agora;
        }
    }
}
