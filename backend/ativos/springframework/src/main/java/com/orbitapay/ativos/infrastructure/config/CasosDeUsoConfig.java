package com.orbitapay.ativos.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.orbitapay.ativos.application.service.PublicadorDeEventosDeAtivo;
import com.orbitapay.ativos.application.service.SimuladorDeMercado;
import com.orbitapay.ativos.application.usecase.AtualizarAtivo;
import com.orbitapay.ativos.application.usecase.ConsultarAtivos;
import com.orbitapay.ativos.application.usecase.ListarAtivoNaBolsa;
import com.orbitapay.ativos.application.usecase.RemoverAtivo;
import com.orbitapay.ativos.application.usecase.SimularCotacoes;
import com.orbitapay.ativos.domain.repository.AtivoRepository;
import com.orbitapay.ativos.domain.repository.BolsaRepository;

@Configuration
@EnableScheduling
public class CasosDeUsoConfig {

    @Bean
    public ListarAtivoNaBolsa listarAtivoNaBolsa(AtivoRepository ativos, BolsaRepository bolsas,
            SimuladorDeMercado simulador, PublicadorDeEventosDeAtivo publicador) {
        return new ListarAtivoNaBolsa(ativos, bolsas, simulador, publicador);
    }

    @Bean
    public AtualizarAtivo atualizarAtivo(AtivoRepository ativos, BolsaRepository bolsas,
            PublicadorDeEventosDeAtivo publicador) {
        return new AtualizarAtivo(ativos, bolsas, publicador);
    }

    @Bean
    public RemoverAtivo removerAtivo(AtivoRepository ativos, PublicadorDeEventosDeAtivo publicador) {
        return new RemoverAtivo(ativos, publicador);
    }

    @Bean
    public ConsultarAtivos consultarAtivos(AtivoRepository ativos, BolsaRepository bolsas) {
        return new ConsultarAtivos(ativos, bolsas);
    }

    @Bean
    public SimularCotacoes simularCotacoes(AtivoRepository ativos, SimuladorDeMercado simulador,
            PublicadorDeEventosDeAtivo publicador) {
        return new SimularCotacoes(ativos, simulador, publicador);
    }
}
