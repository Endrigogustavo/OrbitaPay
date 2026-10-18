package com.orbitapay.pagamentos.application.gateway;

import java.math.BigDecimal;

import com.orbitapay.pagamentos.entity.InstrucoesDePagamento;
import com.orbitapay.pagamentos.entity.MetodoDePagamento;
import com.orbitapay.pagamentos.entity.Pagamento;
import com.orbitapay.pagamentos.entity.SituacaoNoProvedor;

public interface IProvedorDePagamentoGateway {

    InstrucoesDePagamento emitirCobranca(String pagamentoId, BigDecimal valor, MetodoDePagamento metodo);

    SituacaoNoProvedor consultarSituacaoDaCobranca(Pagamento pagamento);
}
