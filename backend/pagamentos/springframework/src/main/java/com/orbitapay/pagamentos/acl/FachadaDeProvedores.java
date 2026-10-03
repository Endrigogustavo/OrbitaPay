package com.orbitapay.pagamentos.acl;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.orbitapay.pagamentos.application.dto.CobrancaEmitida;
import com.orbitapay.pagamentos.application.dto.SituacaoDaCobranca;
import com.orbitapay.pagamentos.application.dto.SolicitacaoDeCobranca;
import com.orbitapay.pagamentos.application.service.ProvedorDePagamentos;
import com.orbitapay.pagamentos.application.service.ProvedorIndisponivelException;
import com.orbitapay.pagamentos.domain.model.MetodoDePagamento;

@Component
public class FachadaDeProvedores implements ProvedorDePagamentos {

    private static final Logger LOG = LoggerFactory.getLogger(FachadaDeProvedores.class);

    private final Map<MetodoDePagamento, AdaptadorDeProvedor> porMetodo = new EnumMap<>(MetodoDePagamento.class);

    public FachadaDeProvedores(List<AdaptadorDeProvedor> adaptadores) {
        for (AdaptadorDeProvedor adaptador : adaptadores) {
            if (porMetodo.containsKey(adaptador.metodo())) {
                throw new IllegalStateException("Dois provedores para " + adaptador.metodo());
            }
            porMetodo.put(adaptador.metodo(), adaptador);
            LOG.info("Provedor de pagamento registrado: {}={}", adaptador.metodo(), adaptador.nome());
        }
    }

    @Override
    public CobrancaEmitida emitir(SolicitacaoDeCobranca solicitacao) {
        AdaptadorDeProvedor adaptador = adaptador(solicitacao.metodo());
        CobrancaEmitida cobranca = adaptador.emitir(solicitacao);
        LOG.info("COBRANÇA EMITIDA | pagamento={} provedor={} referencia={}", solicitacao.pagamentoId(),
                cobranca.provedor(), cobranca.referenciaExterna());
        return cobranca;
    }

    @Override
    public SituacaoDaCobranca consultar(MetodoDePagamento metodo, String referenciaExterna) {
        return adaptador(metodo).consultar(referenciaExterna);
    }

    private AdaptadorDeProvedor adaptador(MetodoDePagamento metodo) {
        AdaptadorDeProvedor adaptador = porMetodo.get(metodo);
        if (adaptador == null) {
            throw new ProvedorIndisponivelException(metodo.rotulo(), "nenhum provedor configurado");
        }
        return adaptador;
    }
}
