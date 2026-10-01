package com.orbitapay.pagamentos.acl;

import com.orbitapay.pagamentos.application.dto.CobrancaEmitida;
import com.orbitapay.pagamentos.application.dto.SituacaoDaCobranca;
import com.orbitapay.pagamentos.application.dto.SolicitacaoDeCobranca;
import com.orbitapay.pagamentos.domain.model.MetodoDePagamento;

/**
 * Um provedor externo visto de dentro da camada anticorrupção. Cada implementação conversa com a API do
 * provedor no formato dele e devolve apenas tipos do OrbitaPay.
 */
public interface AdaptadorDeProvedor {

    MetodoDePagamento metodo();

    String nome();

    CobrancaEmitida emitir(SolicitacaoDeCobranca solicitacao);

    SituacaoDaCobranca consultar(String referenciaExterna);
}
