package com.orbitapay.negociacao.application.usecase.execucao;

import java.time.Instant;

import com.orbitapay.negociacao.application.exception.OperacaoRecusadaException;
import com.orbitapay.negociacao.application.gateway.IOrdemGateway;
import com.orbitapay.negociacao.application.gateway.ITransacaoGateway;
import com.orbitapay.negociacao.entity.Ordem;
import com.orbitapay.negociacao.exception.RegraDeNegocioException;
import com.orbitapay.negociacao.service.OrdemService;

public abstract class ExecucaoDeOrdem {

    protected final OrdemService ordemService;
    protected final IOrdemGateway ordemGateway;
    protected final ITransacaoGateway transacaoGateway;

    protected ExecucaoDeOrdem(OrdemService ordemService, IOrdemGateway ordemGateway, ITransacaoGateway transacaoGateway) {
        this.ordemService = ordemService;
        this.ordemGateway = ordemGateway;
        this.transacaoGateway = transacaoGateway;
    }

    public final Ordem executar(Ordem ordemPendente) {
        try {
            movimentarDinheiroEAcoes(ordemPendente);
        } catch (OperacaoRecusadaException | RegraDeNegocioException recusa) {
            return salvar(ordemService.marcarComoRejeitada(ordemPendente, recusa.getMessage(), Instant.now()));
        }
        return transacaoGateway.executarEmTransacaoERetornar(() -> {
            registrarAcoesNaCarteira(ordemPendente);
            return salvar(ordemService.marcarComoExecutada(ordemPendente, Instant.now()));
        });
    }

    protected abstract void movimentarDinheiroEAcoes(Ordem ordem);

    protected abstract void registrarAcoesNaCarteira(Ordem ordem);

    private Ordem salvar(Ordem ordem) {
        ordemGateway.salvar(ordem);
        return ordem;
    }
}
