package com.orbitapay.pagamentos.adapter.gateway.provedor;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.orbitapay.pagamentos.entity.MetodoDePagamento;

@Component
public class CobrancaTed extends CobrancaSimulada {

    private final Duration tempoAteOPagamentoSerConfirmado;

    public CobrancaTed(@Value("${orbita.provedor-simulado.confirmacao-ted}") Duration tempoAteOPagamentoSerConfirmado) {
        this.tempoAteOPagamentoSerConfirmado = tempoAteOPagamentoSerConfirmado;
    }

    @Override
    public MetodoDePagamento metodoAtendido() {
        return MetodoDePagamento.TED;
    }

    @Override
    protected String gerarCodigoParaOClientePagar(String pagamentoId, BigDecimal valor) {
        return "Banco 999 · Ag 0001 · Conta 99999-9 · Id " + pagamentoId.substring(0, 8).toUpperCase(Locale.ROOT);
    }

    @Override
    protected String descreverComoPagar() {
        return "TED para OrbitaPay S.A. informando o identificador";
    }

    @Override
    protected Duration prazoDeValidadeDaCobranca() {
        return Duration.ofHours(24);
    }

    @Override
    protected Duration tempoAteOPagamentoSerConfirmado() {
        return tempoAteOPagamentoSerConfirmado;
    }
}
