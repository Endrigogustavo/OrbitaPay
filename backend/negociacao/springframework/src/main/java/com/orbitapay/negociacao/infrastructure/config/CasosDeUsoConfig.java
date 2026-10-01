package com.orbitapay.negociacao.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.orbitapay.negociacao.application.service.CatalogoDeAtivos;
import com.orbitapay.negociacao.application.service.PublicadorDeEventosDeOrdem;
import com.orbitapay.negociacao.application.usecase.ConcluirOrdem;
import com.orbitapay.negociacao.application.usecase.ConsultarOrdens;
import com.orbitapay.negociacao.application.usecase.EnviarOrdem;
import com.orbitapay.negociacao.application.usecase.OperacaoComTrava;
import com.orbitapay.negociacao.application.usecase.SincronizarAtivos;
import com.orbitapay.negociacao.application.usecase.SincronizarInvestidores;
import com.orbitapay.negociacao.domain.repository.AtivoNegociavelRepository;
import com.orbitapay.negociacao.domain.repository.InvestidorRepository;
import com.orbitapay.negociacao.domain.repository.OrdemRepository;

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
    public EnviarOrdem enviarOrdem(OrdemRepository ordens, AtivoNegociavelRepository ativos,
            InvestidorRepository investidores, CatalogoDeAtivos catalogo, OperacaoComTrava operacao,
            PublicadorDeEventosDeOrdem publicador, Clock relogio) {
        return new EnviarOrdem(ordens, ativos, investidores, catalogo, operacao, publicador, relogio);
    }

    @Bean
    public ConcluirOrdem concluirOrdem(OrdemRepository ordens, OperacaoComTrava operacao,
            PublicadorDeEventosDeOrdem publicador, Clock relogio) {
        return new ConcluirOrdem(ordens, operacao, publicador, relogio);
    }

    @Bean
    public ConsultarOrdens consultarOrdens(OrdemRepository ordens, AtivoNegociavelRepository ativos) {
        return new ConsultarOrdens(ordens, ativos);
    }

    @Bean
    public SincronizarAtivos sincronizarAtivos(AtivoNegociavelRepository ativos, OperacaoComTrava operacao) {
        return new SincronizarAtivos(ativos, operacao);
    }

    @Bean
    public SincronizarInvestidores sincronizarInvestidores(InvestidorRepository investidores) {
        return new SincronizarInvestidores(investidores);
    }
}
