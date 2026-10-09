package com.orbitapay.negociacao.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.orbitapay.negociacao.application.service.CatalogoDeAtivos;
import com.orbitapay.negociacao.application.service.PublicadorDeEventosDeOrdem;
import com.orbitapay.negociacao.application.usecase.AtualizarCotacoes;
import com.orbitapay.negociacao.application.usecase.CancelarCompra;
import com.orbitapay.negociacao.application.usecase.CancelarVenda;
import com.orbitapay.negociacao.application.usecase.ConfirmarCompra;
import com.orbitapay.negociacao.application.usecase.ConfirmarVenda;
import com.orbitapay.negociacao.application.usecase.ConsultarOrdens;
import com.orbitapay.negociacao.application.usecase.EnviarOrdem;
import com.orbitapay.negociacao.application.usecase.RegistrarAtivo;
import com.orbitapay.negociacao.application.usecase.RegistrarInvestidor;
import com.orbitapay.negociacao.application.usecase.RemoverInvestidor;
import com.orbitapay.negociacao.application.usecase.RetirarAtivo;
import com.orbitapay.negociacao.domain.repository.AtivoNegociavelRepository;
import com.orbitapay.negociacao.domain.repository.InvestidorRepository;
import com.orbitapay.negociacao.domain.repository.OrdemRepository;
import com.orbitapay.negociacao.domain.repository.TravaDeAtivo;

@Configuration
public class CasosDeUsoConfig {

    @Bean
    public EnviarOrdem enviarOrdem(OrdemRepository ordens, AtivoNegociavelRepository ativos,
            InvestidorRepository investidores, CatalogoDeAtivos catalogo, TravaDeAtivo trava,
            PublicadorDeEventosDeOrdem publicador) {
        return new EnviarOrdem(ordens, ativos, investidores, catalogo, trava, publicador);
    }

    @Bean
    public ConfirmarCompra confirmarCompra(OrdemRepository ordens, TravaDeAtivo trava,
            PublicadorDeEventosDeOrdem publicador) {
        return new ConfirmarCompra(ordens, trava, publicador);
    }

    @Bean
    public CancelarCompra cancelarCompra(OrdemRepository ordens, TravaDeAtivo trava,
            PublicadorDeEventosDeOrdem publicador) {
        return new CancelarCompra(ordens, trava, publicador);
    }

    @Bean
    public ConfirmarVenda confirmarVenda(OrdemRepository ordens, TravaDeAtivo trava,
            PublicadorDeEventosDeOrdem publicador) {
        return new ConfirmarVenda(ordens, trava, publicador);
    }

    @Bean
    public CancelarVenda cancelarVenda(OrdemRepository ordens, PublicadorDeEventosDeOrdem publicador) {
        return new CancelarVenda(ordens, publicador);
    }

    @Bean
    public ConsultarOrdens consultarOrdens(OrdemRepository ordens, AtivoNegociavelRepository ativos) {
        return new ConsultarOrdens(ordens, ativos);
    }

    @Bean
    public RegistrarAtivo registrarAtivo(AtivoNegociavelRepository ativos, TravaDeAtivo trava) {
        return new RegistrarAtivo(ativos, trava);
    }

    @Bean
    public RetirarAtivo retirarAtivo(AtivoNegociavelRepository ativos, TravaDeAtivo trava) {
        return new RetirarAtivo(ativos, trava);
    }

    @Bean
    public AtualizarCotacoes atualizarCotacoes(AtivoNegociavelRepository ativos) {
        return new AtualizarCotacoes(ativos);
    }

    @Bean
    public RegistrarInvestidor registrarInvestidor(InvestidorRepository investidores) {
        return new RegistrarInvestidor(investidores);
    }

    @Bean
    public RemoverInvestidor removerInvestidor(InvestidorRepository investidores) {
        return new RemoverInvestidor(investidores);
    }
}
