package com.orbitapay.negociacao.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.orbitapay.negociacao.application.port.in.ConcluirOrdemUseCase;
import com.orbitapay.negociacao.application.port.in.ConsultarOrdensUseCase;
import com.orbitapay.negociacao.application.port.in.EnviarOrdemUseCase;
import com.orbitapay.negociacao.application.port.in.SincronizarAtivosUseCase;
import com.orbitapay.negociacao.application.port.in.SincronizarInvestidoresUseCase;
import com.orbitapay.negociacao.application.port.out.AtivoNegociavelRepository;
import com.orbitapay.negociacao.application.port.out.CatalogoDeAtivos;
import com.orbitapay.negociacao.application.port.out.InvestidorRepository;
import com.orbitapay.negociacao.application.port.out.OrdemRepository;
import com.orbitapay.negociacao.application.port.out.PublicadorDeEventosDeOrdem;
import com.orbitapay.negociacao.application.usecase.ConcluirOrdem;
import com.orbitapay.negociacao.application.usecase.ConsultarOrdens;
import com.orbitapay.negociacao.application.usecase.EnviarOrdem;
import com.orbitapay.negociacao.application.usecase.OperacaoComTrava;
import com.orbitapay.negociacao.application.usecase.SincronizarAtivos;
import com.orbitapay.negociacao.application.usecase.SincronizarInvestidores;

@Configuration
public class CasosDeUsoConfig {

    @Bean
    public Clock relogio() {
        return Clock.systemUTC();
    }

    @Bean
    public OperacaoComTrava operacaoComTrava(AtivoNegociavelRepository ativos) {
        return new OperacaoComTrava(ativos);
    }

    @Bean
    public EnviarOrdemUseCase enviarOrdem(OrdemRepository ordens, AtivoNegociavelRepository ativos,
            InvestidorRepository investidores, CatalogoDeAtivos catalogo, OperacaoComTrava operacao,
            PublicadorDeEventosDeOrdem publicador, Clock relogio) {
        return new EnviarOrdem(ordens, ativos, investidores, catalogo, operacao, publicador, relogio);
    }

    @Bean
    public ConcluirOrdemUseCase concluirOrdem(OrdemRepository ordens, OperacaoComTrava operacao,
            PublicadorDeEventosDeOrdem publicador, Clock relogio) {
        return new ConcluirOrdem(ordens, operacao, publicador, relogio);
    }

    @Bean
    public ConsultarOrdensUseCase consultarOrdens(OrdemRepository ordens, AtivoNegociavelRepository ativos) {
        return new ConsultarOrdens(ordens, ativos);
    }

    @Bean
    public SincronizarAtivosUseCase sincronizarAtivos(AtivoNegociavelRepository ativos, OperacaoComTrava operacao) {
        return new SincronizarAtivos(ativos, operacao);
    }

    @Bean
    public SincronizarInvestidoresUseCase sincronizarInvestidores(InvestidorRepository investidores) {
        return new SincronizarInvestidores(investidores);
    }
}
