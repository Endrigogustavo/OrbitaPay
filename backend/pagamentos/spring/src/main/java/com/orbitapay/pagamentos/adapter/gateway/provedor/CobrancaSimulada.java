package com.orbitapay.pagamentos.adapter.gateway.provedor;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import com.orbitapay.pagamentos.entity.InstrucoesDePagamento;
import com.orbitapay.pagamentos.entity.MetodoDePagamento;
import com.orbitapay.pagamentos.entity.Pagamento;
import com.orbitapay.pagamentos.entity.SituacaoNoProvedor;

public abstract class CobrancaSimulada {

    public final InstrucoesDePagamento emitirCobranca(String pagamentoId, BigDecimal valor) {
        return new InstrucoesDePagamento(gerarCodigoParaOClientePagar(pagamentoId, valor), descreverComoPagar(),
                Instant.now().plus(prazoDeValidadeDaCobranca()));
    }

    public final SituacaoNoProvedor consultarSituacaoDaCobranca(Pagamento pagamento) {
        Instant agora = Instant.now();
        if (agora.isAfter(pagamento.criadoEm().plus(tempoAteOPagamentoSerConfirmado()))) {
            return SituacaoNoProvedor.PAGO;
        }
        if (agora.isAfter(pagamento.instrucoes().validoAte())) {
            return SituacaoNoProvedor.EXPIRADO;
        }
        return SituacaoNoProvedor.AGUARDANDO;
    }

    public abstract MetodoDePagamento metodoAtendido();

    protected abstract String gerarCodigoParaOClientePagar(String pagamentoId, BigDecimal valor);

    protected abstract String descreverComoPagar();

    protected abstract Duration prazoDeValidadeDaCobranca();

    protected abstract Duration tempoAteOPagamentoSerConfirmado();
}
