package com.orbitapay.pagamentos.adapter.gateway.provedor;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.orbitapay.pagamentos.entity.MetodoDePagamento;

@Component
public class CobrancaBoleto extends CobrancaSimulada {

    private final Duration tempoAteOPagamentoSerConfirmado;

    public CobrancaBoleto(
            @Value("${orbita.provedor-simulado.confirmacao-boleto}") Duration tempoAteOPagamentoSerConfirmado) {
        this.tempoAteOPagamentoSerConfirmado = tempoAteOPagamentoSerConfirmado;
    }

    @Override
    public MetodoDePagamento metodoAtendido() {
        return MetodoDePagamento.BOLETO;
    }

    @Override
    protected String gerarCodigoParaOClientePagar(String pagamentoId, BigDecimal valor) {
        ThreadLocalRandom aleatorio = ThreadLocalRandom.current();
        long valorEmCentavos = valor.movePointRight(2).longValue();
        return String.format("99990.%05d %05d.%06d %05d.%06d 1 %014d", aleatorio.nextInt(100000),
                aleatorio.nextInt(100000), aleatorio.nextInt(1000000), aleatorio.nextInt(100000),
                aleatorio.nextInt(1000000), valorEmCentavos);
    }

    @Override
    protected String descreverComoPagar() {
        return "Linha digitável · vence em 3 dias";
    }

    @Override
    protected Duration prazoDeValidadeDaCobranca() {
        return Duration.ofDays(3);
    }

    @Override
    protected Duration tempoAteOPagamentoSerConfirmado() {
        return tempoAteOPagamentoSerConfirmado;
    }
}
