package com.orbitapay.pagamentos.application.service;

import com.orbitapay.pagamentos.application.dto.CobrancaEmitida;
import com.orbitapay.pagamentos.application.dto.SituacaoDaCobranca;
import com.orbitapay.pagamentos.application.dto.SolicitacaoDeCobranca;
import com.orbitapay.pagamentos.domain.model.MetodoDePagamento;

/**
 * Fachada única para os provedores externos (PSP Pix, banco emissor de boletos, SPB para TED). É implementada
 * pela camada anticorrupção, que escolhe o provedor pelo método e traduz os modelos de cada um.
 *
 * @throws ProvedorIndisponivelException quando o provedor não responde ou recusa a operação
 */
public interface ProvedorDePagamentos {

    CobrancaEmitida emitir(SolicitacaoDeCobranca solicitacao);

    SituacaoDaCobranca consultar(MetodoDePagamento metodo, String referenciaExterna);
}
