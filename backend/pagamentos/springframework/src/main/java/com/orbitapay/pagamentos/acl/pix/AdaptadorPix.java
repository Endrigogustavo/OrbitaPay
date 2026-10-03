package com.orbitapay.pagamentos.acl.pix;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.stereotype.Component;

import com.orbitapay.pagamentos.acl.AclProperties;
import com.orbitapay.pagamentos.acl.AdaptadorDeProvedor;
import com.orbitapay.pagamentos.application.dto.CobrancaEmitida;
import com.orbitapay.pagamentos.application.dto.SituacaoDaCobranca;
import com.orbitapay.pagamentos.application.dto.SolicitacaoDeCobranca;
import com.orbitapay.pagamentos.application.service.ProvedorIndisponivelException;
import com.orbitapay.pagamentos.domain.model.Dinheiro;
import com.orbitapay.pagamentos.domain.model.InstrucoesDePagamento;
import com.orbitapay.pagamentos.domain.model.MetodoDePagamento;
import com.orbitapay.pagamentos.provedor.ErroDoProvedor;
import com.orbitapay.pagamentos.provedor.pix.PspPixSimulado;
import com.orbitapay.pagamentos.provedor.pix.modelo.CobrancaImediata;

@Component
public class AdaptadorPix implements AdaptadorDeProvedor {

    private static final String NOME = "PSP Pix (simulado)";

    private final PspPixSimulado psp;
    private final AclProperties.Pix config;

    public AdaptadorPix(PspPixSimulado psp, AclProperties propriedades) {
        this.psp = psp;
        this.config = propriedades.pix();
    }

    @Override
    public MetodoDePagamento metodo() {
        return MetodoDePagamento.PIX;
    }

    @Override
    public String nome() {
        return NOME;
    }

    @Override
    public CobrancaEmitida emitir(SolicitacaoDeCobranca solicitacao) {
        CobrancaImediata.Requisicao requisicao = new CobrancaImediata.Requisicao(
                new CobrancaImediata.Calendario(null, config.expiracaoSegundos()),
                new CobrancaImediata.Valor(solicitacao.valor().valor().toPlainString()),
                config.chave(),
                "Depósito na conta OrbitaPay · " + solicitacao.pagamentoId());
        CobrancaImediata.Resposta resposta;
        try {
            resposta = psp.criarCobranca(requisicao);
        } catch (ErroDoProvedor e) {
            throw falhaDoProvedor(e);
        }
        Instant criacao = Instant.parse(resposta.calendario().criacao());
        return new CobrancaEmitida(NOME, resposta.txid(), new InstrucoesDePagamento(resposta.pixCopiaECola(),
                "Pix copia e cola", criacao.plusSeconds(resposta.calendario().expiracao())));
    }

    @Override
    public SituacaoDaCobranca consultar(String txid) {
        CobrancaImediata.Resposta resposta;
        try {
            resposta = psp.consultarCobranca(txid, config.chave());
        } catch (ErroDoProvedor e) {
            throw falhaDoProvedor(e);
        }
        return switch (resposta.status()) {
            case CobrancaImediata.ATIVA -> SituacaoDaCobranca.aguardando();
            case CobrancaImediata.CONCLUIDA -> {
                CobrancaImediata.PixRecebido pix = resposta.pix().getFirst();
                yield SituacaoDaCobranca.paga(new Dinheiro(new BigDecimal(pix.valor())), Instant.parse(pix.horario()));
            }
            case CobrancaImediata.REMOVIDA_PELO_PSP, "REMOVIDA_PELO_USUARIO_RECEBEDOR" -> SituacaoDaCobranca.expirada();
            default -> throw new ProvedorIndisponivelException(NOME, "status desconhecido " + resposta.status());
        };
    }

    private static ProvedorIndisponivelException falhaDoProvedor(ErroDoProvedor e) {
        return new ProvedorIndisponivelException(NOME, e.codigo() + " · " + e.getMessage());
    }
}
