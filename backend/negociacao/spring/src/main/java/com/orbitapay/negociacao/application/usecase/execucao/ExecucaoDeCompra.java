package com.orbitapay.negociacao.application.usecase.execucao;

import com.orbitapay.negociacao.application.exception.OperacaoRecusadaException;
import com.orbitapay.negociacao.application.gateway.IAtivoGateway;
import com.orbitapay.negociacao.application.gateway.IContaGateway;
import com.orbitapay.negociacao.application.gateway.IOrdemGateway;
import com.orbitapay.negociacao.application.gateway.ITransacaoGateway;
import com.orbitapay.negociacao.application.usecase.CarteiraUseCase;
import com.orbitapay.negociacao.entity.Ordem;
import com.orbitapay.negociacao.service.OrdemService;

public class ExecucaoDeCompra extends ExecucaoDeOrdem {

    private final IAtivoGateway ativoGateway;
    private final IContaGateway contaGateway;
    private final CarteiraUseCase carteiraUseCase;

    public ExecucaoDeCompra(OrdemService ordemService, IOrdemGateway ordemGateway, ITransacaoGateway transacaoGateway,
            IAtivoGateway ativoGateway, IContaGateway contaGateway, CarteiraUseCase carteiraUseCase) {
        super(ordemService, ordemGateway, transacaoGateway);
        this.ativoGateway = ativoGateway;
        this.contaGateway = contaGateway;
        this.carteiraUseCase = carteiraUseCase;
    }

    @Override
    protected void movimentarDinheiroEAcoes(Ordem ordem) {
        ativoGateway.retirarAcoesDoEstoque(ordem.ticker(), ordem.quantidade());
        try {
            contaGateway.debitarCompraDeAcoes(ordem.clienteId(), ordem.id(), ordem.valorTotal(),
                    "Compra · " + ordem.quantidade() + " " + ordem.ticker());
        } catch (OperacaoRecusadaException debitoRecusado) {
            ativoGateway.devolverAcoesAoEstoque(ordem.ticker(), ordem.quantidade());
            throw debitoRecusado;
        } catch (RuntimeException falhaAoDebitar) {
            contaGateway.estornarCompraDeAcoes(ordem.clienteId(), ordem.id());
            ativoGateway.devolverAcoesAoEstoque(ordem.ticker(), ordem.quantidade());
            throw new OperacaoRecusadaException("Não foi possível debitar sua conta agora. Tente de novo");
        }
    }

    @Override
    protected void registrarAcoesNaCarteira(Ordem ordem) {
        carteiraUseCase.adicionarAcoesCompradas(ordem.clienteId(), ordem.ticker(), ordem.quantidade(),
                ordem.precoUnitario());
    }
}
