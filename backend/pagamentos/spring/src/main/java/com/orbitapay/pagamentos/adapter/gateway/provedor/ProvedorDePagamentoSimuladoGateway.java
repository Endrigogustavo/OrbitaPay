package com.orbitapay.pagamentos.adapter.gateway.provedor;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.orbitapay.pagamentos.entity.InstrucoesDePagamento;
import com.orbitapay.pagamentos.entity.MetodoDePagamento;
import com.orbitapay.pagamentos.entity.Pagamento;
import com.orbitapay.pagamentos.entity.SituacaoNoProvedor;
import com.orbitapay.pagamentos.application.gateway.IProvedorDePagamentoGateway;

@Component
public class ProvedorDePagamentoSimuladoGateway implements IProvedorDePagamentoGateway {

    private final Map<MetodoDePagamento, CobrancaSimulada> cobrancaPorMetodo = new EnumMap<>(MetodoDePagamento.class);

    public ProvedorDePagamentoSimuladoGateway(List<CobrancaSimulada> cobrancasDisponiveis) {
        for (CobrancaSimulada cobranca : cobrancasDisponiveis) {
            cobrancaPorMetodo.put(cobranca.metodoAtendido(), cobranca);
        }
    }

    @Override
    public InstrucoesDePagamento emitirCobranca(String pagamentoId, BigDecimal valor, MetodoDePagamento metodo) {
        return cobrancaPorMetodo.get(metodo).emitirCobranca(pagamentoId, valor);
    }

    @Override
    public SituacaoNoProvedor consultarSituacaoDaCobranca(Pagamento pagamento) {
        return cobrancaPorMetodo.get(pagamento.metodo()).consultarSituacaoDaCobranca(pagamento);
    }
}
