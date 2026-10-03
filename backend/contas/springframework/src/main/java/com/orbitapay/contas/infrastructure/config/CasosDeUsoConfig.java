package com.orbitapay.contas.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.orbitapay.contas.application.service.GeradorDeNumeroDeConta;
import com.orbitapay.contas.application.service.PublicadorDeEventosDeConta;
import com.orbitapay.contas.application.usecase.AbrirConta;
import com.orbitapay.contas.application.usecase.ConsultarContas;
import com.orbitapay.contas.application.usecase.CreditarDeposito;
import com.orbitapay.contas.application.usecase.LiquidarOrdem;
import com.orbitapay.contas.application.usecase.Sacar;
import com.orbitapay.contas.application.usecase.SincronizarTitular;
import com.orbitapay.contas.domain.repository.ContaRepository;

@Configuration
public class CasosDeUsoConfig {

    @Bean
    public AbrirConta abrirConta(ContaRepository repositorio, GeradorDeNumeroDeConta gerador) {
        return new AbrirConta(repositorio, gerador);
    }

    @Bean
    public CreditarDeposito creditarDeposito(ContaRepository repositorio) {
        return new CreditarDeposito(repositorio);
    }

    @Bean
    public Sacar sacar(ContaRepository repositorio) {
        return new Sacar(repositorio);
    }

    @Bean
    public ConsultarContas consultarContas(ContaRepository repositorio) {
        return new ConsultarContas(repositorio);
    }

    @Bean
    public LiquidarOrdem liquidarOrdem(ContaRepository repositorio, PublicadorDeEventosDeConta publicador) {
        return new LiquidarOrdem(repositorio, publicador);
    }

    @Bean
    public SincronizarTitular sincronizarTitular(ContaRepository repositorio) {
        return new SincronizarTitular(repositorio);
    }
}
