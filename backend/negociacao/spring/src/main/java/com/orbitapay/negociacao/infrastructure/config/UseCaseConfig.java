package com.orbitapay.negociacao.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.orbitapay.negociacao.application.gateway.IAtivoGateway;
import com.orbitapay.negociacao.application.gateway.ICarteiraGateway;
import com.orbitapay.negociacao.application.gateway.IClienteGateway;
import com.orbitapay.negociacao.application.gateway.IContaGateway;
import com.orbitapay.negociacao.application.gateway.IEventoGateway;
import com.orbitapay.negociacao.application.gateway.IOrdemGateway;
import com.orbitapay.negociacao.application.gateway.ITransacaoGateway;
import com.orbitapay.negociacao.application.usecase.CarteiraUseCase;
import com.orbitapay.negociacao.application.usecase.OrdemUseCase;
import com.orbitapay.negociacao.application.usecase.execucao.ExecucaoDeCompra;
import com.orbitapay.negociacao.application.usecase.execucao.ExecucaoDeVenda;
import com.orbitapay.negociacao.service.CarteiraService;
import com.orbitapay.negociacao.service.OrdemService;

@Configuration
public class UseCaseConfig {

    @Bean
    public OrdemService ordemService() {
        return new OrdemService();
    }

    @Bean
    public CarteiraService carteiraService() {
        return new CarteiraService();
    }

    @Bean
    public CarteiraUseCase carteiraUseCase(CarteiraService carteiraService, ICarteiraGateway carteiraGateway,
            IAtivoGateway ativoGateway, ITransacaoGateway transacaoGateway) {
        return new CarteiraUseCase(carteiraService, carteiraGateway, ativoGateway, transacaoGateway);
    }

    @Bean
    public ExecucaoDeCompra execucaoDeCompra(OrdemService ordemService, IOrdemGateway ordemGateway,
            ITransacaoGateway transacaoGateway, IAtivoGateway ativoGateway, IContaGateway contaGateway,
            CarteiraUseCase carteiraUseCase) {
        return new ExecucaoDeCompra(ordemService, ordemGateway, transacaoGateway, ativoGateway, contaGateway,
                carteiraUseCase);
    }

    @Bean
    public ExecucaoDeVenda execucaoDeVenda(OrdemService ordemService, IOrdemGateway ordemGateway,
            ITransacaoGateway transacaoGateway, IAtivoGateway ativoGateway, IContaGateway contaGateway,
            CarteiraUseCase carteiraUseCase) {
        return new ExecucaoDeVenda(ordemService, ordemGateway, transacaoGateway, ativoGateway, contaGateway,
                carteiraUseCase);
    }

    @Bean
    public OrdemUseCase ordemUseCase(OrdemService ordemService, IOrdemGateway ordemGateway, IAtivoGateway ativoGateway,
            IClienteGateway clienteGateway, IEventoGateway eventoGateway, ExecucaoDeCompra execucaoDeCompra,
            ExecucaoDeVenda execucaoDeVenda) {
        return new OrdemUseCase(ordemService, ordemGateway, ativoGateway, clienteGateway, eventoGateway,
                execucaoDeCompra, execucaoDeVenda);
    }
}
