package com.orbitapay.pagamentos.acl;

import com.orbitapay.pagamentos.application.dto.CobrancaEmitida;
import com.orbitapay.pagamentos.application.dto.SituacaoDaCobranca;
import com.orbitapay.pagamentos.application.dto.SolicitacaoDeCobranca;
import com.orbitapay.pagamentos.domain.model.MetodoDePagamento;

public interface AdaptadorDeProvedor {

    MetodoDePagamento metodo();

    String nome();

    CobrancaEmitida emitir(SolicitacaoDeCobranca solicitacao);

    SituacaoDaCobranca consultar(String referenciaExterna);
}
