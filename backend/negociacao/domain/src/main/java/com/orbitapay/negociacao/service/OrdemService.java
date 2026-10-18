package com.orbitapay.negociacao.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

import com.orbitapay.negociacao.entity.CotacaoDoAtivo;
import com.orbitapay.negociacao.entity.Ordem;
import com.orbitapay.negociacao.entity.StatusOrdem;
import com.orbitapay.negociacao.entity.TipoOrdem;
import com.orbitapay.negociacao.exception.ClienteBloqueadoException;
import com.orbitapay.negociacao.exception.RegraDeNegocioException;

public class OrdemService {

    private static final int QUANTIDADE_MAXIMA_POR_ORDEM = 9999;

    public void garantirQueClientePodeNegociar(boolean clienteBloqueado) {
        if (clienteBloqueado) {
            throw new ClienteBloqueadoException();
        }
    }

    public TipoOrdem converterTipoDaOrdem(String tipoInformado) {
        for (TipoOrdem tipo : TipoOrdem.values()) {
            if (tipo.name().equalsIgnoreCase(tipoInformado)) {
                return tipo;
            }
        }
        throw new RegraDeNegocioException("Tipo de ordem inválido: " + tipoInformado);
    }

    public int validarQuantidade(Integer quantidade) {
        if (quantidade == null || quantidade < 1 || quantidade > QUANTIDADE_MAXIMA_POR_ORDEM) {
            throw new RegraDeNegocioException("Quantidade deve ficar entre 1 e " + QUANTIDADE_MAXIMA_POR_ORDEM + " ações");
        }
        return quantidade;
    }

    public Ordem criarOrdemPendente(String ordemId, String clienteId, CotacaoDoAtivo ativo, TipoOrdem tipo,
            int quantidade, Instant agora) {
        BigDecimal valorTotalEmReais = ativo.cotacao().multiply(BigDecimal.valueOf(quantidade))
                .multiply(ativo.cambio()).setScale(2, RoundingMode.HALF_EVEN);
        return new Ordem(ordemId, clienteId, ativo.ticker(), tipo, quantidade, ativo.cotacao(), ativo.moeda(),
                ativo.cambio(), valorTotalEmReais, StatusOrdem.PENDENTE, null, agora, agora);
    }

    public Ordem marcarComoExecutada(Ordem ordem, Instant agora) {
        return mudarStatus(ordem, StatusOrdem.EXECUTADA, null, agora);
    }

    public Ordem marcarComoRejeitada(Ordem ordem, String motivo, Instant agora) {
        return mudarStatus(ordem, StatusOrdem.REJEITADA, motivo, agora);
    }

    private static Ordem mudarStatus(Ordem ordem, StatusOrdem novoStatus, String motivoRejeicao, Instant agora) {
        if (ordem.status() != StatusOrdem.PENDENTE) {
            throw new RegraDeNegocioException("A ordem " + ordem.id() + " já foi finalizada como " + ordem.status());
        }
        return new Ordem(ordem.id(), ordem.clienteId(), ordem.ticker(), ordem.tipo(), ordem.quantidade(),
                ordem.precoUnitario(), ordem.moeda(), ordem.cambio(), ordem.valorTotal(), novoStatus, motivoRejeicao,
                ordem.criadaEm(), agora);
    }
}
