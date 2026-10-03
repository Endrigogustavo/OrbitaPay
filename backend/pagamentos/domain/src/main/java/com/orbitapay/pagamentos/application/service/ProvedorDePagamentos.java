package com.orbitapay.pagamentos.application.service;

import com.orbitapay.pagamentos.application.dto.CobrancaEmitida;
import com.orbitapay.pagamentos.application.dto.SituacaoDaCobranca;
import com.orbitapay.pagamentos.application.dto.SolicitacaoDeCobranca;
import com.orbitapay.pagamentos.domain.model.MetodoDePagamento;

public interface ProvedorDePagamentos {

    CobrancaEmitida emitir(SolicitacaoDeCobranca solicitacao);

    SituacaoDaCobranca consultar(MetodoDePagamento metodo, String referenciaExterna);
}
