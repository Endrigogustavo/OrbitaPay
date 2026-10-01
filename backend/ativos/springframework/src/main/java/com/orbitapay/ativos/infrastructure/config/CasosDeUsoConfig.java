package com.orbitapay.ativos.infrastructure.config;

import java.time.Clock;

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
    public Clock relogio() {
        return Clock.systemUTC();
    }

    @Bean
    public ListarAtivoNaBolsa listarAtivoNaBolsa(AtivoRepository ativos, BolsaRepository bolsas,
            SimuladorDeMercado simulador, PublicadorDeEventosDeAtivo publicador, Clock relogio) {
        return new ListarAtivoNaBolsa(ativos, bolsas, simulador, publicador, relogio);
    }

    @Bean
    public AtualizarAtivo atualizarAtivo(AtivoRepository ativos, BolsaRepository bolsas,
            PublicadorDeEventosDeAtivo publicador, Clock relogio) {
        return new AtualizarAtivo(ativos, bolsas, publicador, relogio);
    }

    @Bean
    public RemoverAtivo removerAtivo(AtivoRepository ativos, PublicadorDeEventosDeAtivo publicador,
            Clock relogio) {
        return new RemoverAtivo(ativos, publicador, relogio);
    }

    @Bean
    public ConsultarAtivos consultarAtivos(AtivoRepository ativos, BolsaRepository bolsas) {
        return new ConsultarAtivos(ativos, bolsas);
    }

    @Bean
    public SimularCotacoes simularCotacoes(AtivoRepository ativos, SimuladorDeMercado simulador,
            PublicadorDeEventosDeAtivo publicador, Clock relogio) {
        return new SimularCotacoes(ativos, simulador, publicador, relogio);
    }
}
