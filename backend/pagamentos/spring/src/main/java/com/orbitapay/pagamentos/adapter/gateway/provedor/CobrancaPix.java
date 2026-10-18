package com.orbitapay.pagamentos.adapter.gateway.provedor;

import java.math.BigDecimal;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.orbitapay.pagamentos.entity.MetodoDePagamento;

@Component
public class CobrancaPix extends CobrancaSimulada {

    private final Duration tempoAteOPagamentoSerConfirmado;

    public CobrancaPix(@Value("${orbita.provedor-simulado.confirmacao-pix}") Duration tempoAteOPagamentoSerConfirmado) {
        this.tempoAteOPagamentoSerConfirmado = tempoAteOPagamentoSerConfirmado;
    }

    @Override
    public MetodoDePagamento metodoAtendido() {
        return MetodoDePagamento.PIX;
    }

    @Override
    protected String gerarCodigoParaOClientePagar(String pagamentoId, BigDecimal valor) {
        return "00020126580014br.gov.bcb.pix0136" + pagamentoId + "5204000053039865406" + valor.toPlainString()
                + "5802BR5915OrbitaPay S.A.6009SAO PAULO6304";
    }

    @Override
    protected String descreverComoPagar() {
        return "Pix copia e cola";
    }

    @Override
    protected Duration prazoDeValidadeDaCobranca() {
        return Duration.ofMinutes(30);
    }

    @Override
    protected Duration tempoAteOPagamentoSerConfirmado() {
        return tempoAteOPagamentoSerConfirmado;
    }
}
