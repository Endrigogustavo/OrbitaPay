package com.orbitapay.ativos.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.orbitapay.ativos.application.port.in.AtualizarAtivoUseCase;
import com.orbitapay.ativos.application.port.in.ConsultarAtivosUseCase;
import com.orbitapay.ativos.application.port.in.ListarAtivoNaBolsaUseCase;
import com.orbitapay.ativos.application.port.in.RemoverAtivoUseCase;
import com.orbitapay.ativos.application.port.in.SimularCotacoesUseCase;
import com.orbitapay.ativos.application.port.out.AtivoRepository;
import com.orbitapay.ativos.application.port.out.BolsaRepository;
import com.orbitapay.ativos.application.port.out.PublicadorDeEventosDeAtivo;
import com.orbitapay.ativos.application.port.out.SimuladorDeMercado;
import com.orbitapay.ativos.application.usecase.AtualizarAtivo;
import com.orbitapay.ativos.application.usecase.ConsultarAtivos;
import com.orbitapay.ativos.application.usecase.ListarAtivoNaBolsa;
import com.orbitapay.ativos.application.usecase.RemoverAtivo;
import com.orbitapay.ativos.application.usecase.SimularCotacoes;

@Configuration
@EnableScheduling
public class CasosDeUsoConfig {

    @Bean
    public Clock relogio() {
        return Clock.systemUTC();
    }

    @Bean
    public ListarAtivoNaBolsaUseCase listarAtivoNaBolsa(AtivoRepository ativos, BolsaRepository bolsas,
            SimuladorDeMercado simulador, PublicadorDeEventosDeAtivo publicador, Clock relogio) {
        return new ListarAtivoNaBolsa(ativos, bolsas, simulador, publicador, relogio);
    }

    @Bean
    public AtualizarAtivoUseCase atualizarAtivo(AtivoRepository ativos, BolsaRepository bolsas,
            PublicadorDeEventosDeAtivo publicador, Clock relogio) {
        return new AtualizarAtivo(ativos, bolsas, publicador, relogio);
    }

    @Bean
    public RemoverAtivoUseCase removerAtivo(AtivoRepository ativos, PublicadorDeEventosDeAtivo publicador,
            Clock relogio) {
        return new RemoverAtivo(ativos, publicador, relogio);
    }

    @Bean
    public ConsultarAtivosUseCase consultarAtivos(AtivoRepository ativos, BolsaRepository bolsas) {
        return new ConsultarAtivos(ativos, bolsas);
    }

    @Bean
    public SimularCotacoesUseCase simularCotacoes(AtivoRepository ativos, SimuladorDeMercado simulador,
            PublicadorDeEventosDeAtivo publicador, Clock relogio) {
        return new SimularCotacoes(ativos, simulador, publicador, relogio);
    }
}
