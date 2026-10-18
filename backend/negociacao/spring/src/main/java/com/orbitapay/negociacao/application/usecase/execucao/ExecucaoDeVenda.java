package com.orbitapay.negociacao.application.usecase.execucao;

import com.orbitapay.negociacao.application.exception.OperacaoRecusadaException;
import com.orbitapay.negociacao.application.gateway.IAtivoGateway;
import com.orbitapay.negociacao.application.gateway.IContaGateway;
import com.orbitapay.negociacao.application.gateway.IOrdemGateway;
import com.orbitapay.negociacao.application.gateway.ITransacaoGateway;
import com.orbitapay.negociacao.application.usecase.CarteiraUseCase;
import com.orbitapay.negociacao.entity.Ordem;
import com.orbitapay.negociacao.service.OrdemService;

public class ExecucaoDeVenda extends ExecucaoDeOrdem {

    private final IAtivoGateway ativoGateway;
    private final IContaGateway contaGateway;
    private final CarteiraUseCase carteiraUseCase;

    public ExecucaoDeVenda(OrdemService ordemService, IOrdemGateway ordemGateway, ITransacaoGateway transacaoGateway,
            IAtivoGateway ativoGateway, IContaGateway contaGateway, CarteiraUseCase carteiraUseCase) {
        super(ordemService, ordemGateway, transacaoGateway);
        this.ativoGateway = ativoGateway;
        this.contaGateway = contaGateway;
        this.carteiraUseCase = carteiraUseCase;
    }

    @Override
    protected void movimentarDinheiroEAcoes(Ordem ordem) {
        carteiraUseCase.retirarAcoesVendidas(ordem.clienteId(), ordem.ticker(), ordem.quantidade());
        try {
            contaGateway.creditarVendaDeAcoes(ordem.clienteId(), ordem.id(), ordem.valorTotal(),
                    "Venda · " + ordem.quantidade() + " " + ordem.ticker());
        } catch (RuntimeException falhaAoCreditar) {
            carteiraUseCase.devolverAcoesDeVendaCancelada(ordem.clienteId(), ordem.ticker(), ordem.quantidade());
            throw new OperacaoRecusadaException("Não foi possível creditar a venda na sua conta. As ações foram devolvidas");
        }
        ativoGateway.devolverAcoesAoEstoque(ordem.ticker(), ordem.quantidade());
    }

    @Override
    protected void registrarAcoesNaCarteira(Ordem ordem) {
    }
}
