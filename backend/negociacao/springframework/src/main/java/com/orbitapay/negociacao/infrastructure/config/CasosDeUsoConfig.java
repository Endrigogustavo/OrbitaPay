package com.orbitapay.negociacao.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.orbitapay.negociacao.application.service.CatalogoDeAtivos;
import com.orbitapay.negociacao.application.service.PublicadorDeEventosDeOrdem;
import com.orbitapay.negociacao.application.usecase.ConcluirOrdem;
import com.orbitapay.negociacao.application.usecase.ConsultarOrdens;
import com.orbitapay.negociacao.application.usecase.EnviarOrdem;
import com.orbitapay.negociacao.application.usecase.SincronizarAtivos;
import com.orbitapay.negociacao.application.usecase.SincronizarInvestidores;
import com.orbitapay.negociacao.domain.repository.AtivoNegociavelRepository;
import com.orbitapay.negociacao.domain.repository.InvestidorRepository;
import com.orbitapay.negociacao.domain.repository.OrdemRepository;

@Configuration
public class CasosDeUsoConfig {

    @Bean
    public EnviarOrdem enviarOrdem(OrdemRepository ordens, AtivoNegociavelRepository ativos,
            InvestidorRepository investidores, CatalogoDeAtivos catalogo, PublicadorDeEventosDeOrdem publicador) {
        return new EnviarOrdem(ordens, ativos, investidores, catalogo, publicador);
    }

    @Bean
    public ConcluirOrdem concluirOrdem(OrdemRepository ordens, AtivoNegociavelRepository ativos,
            PublicadorDeEventosDeOrdem publicador) {
        return new ConcluirOrdem(ordens, ativos, publicador);
    }

    @Bean
    public ConsultarOrdens consultarOrdens(OrdemRepository ordens, AtivoNegociavelRepository ativos) {
        return new ConsultarOrdens(ordens, ativos);
    }

    @Bean
    public SincronizarAtivos sincronizarAtivos(AtivoNegociavelRepository ativos) {
        return new SincronizarAtivos(ativos);
    }

    @Bean
    public SincronizarInvestidores sincronizarInvestidores(InvestidorRepository investidores) {
        return new SincronizarInvestidores(investidores);
    }
}
