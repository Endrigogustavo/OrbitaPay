package com.orbitapay.contas.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.orbitapay.contas.application.service.GeradorDeNumeroDeConta;
import com.orbitapay.contas.application.service.PublicadorDeEventosDeConta;
import com.orbitapay.contas.application.usecase.AbrirConta;
import com.orbitapay.contas.application.usecase.ConsultarContas;
import com.orbitapay.contas.application.usecase.CreditarDeposito;
import com.orbitapay.contas.application.usecase.LiquidarOrdem;
import com.orbitapay.contas.application.usecase.OperacaoComTrava;
import com.orbitapay.contas.application.usecase.Sacar;
import com.orbitapay.contas.application.usecase.SincronizarTitular;
import com.orbitapay.contas.domain.repository.ContaRepository;

@Configuration
public class CasosDeUsoConfig {

    @Bean
    public Clock relogio() {
        return Clock.systemUTC();
    }

    @Bean
    public OperacaoComTrava operacaoComTrava(ContaRepository repositorio) {
        return new OperacaoComTrava(repositorio);
    }

    @Bean
    public AbrirConta abrirConta(ContaRepository repositorio, GeradorDeNumeroDeConta gerador, Clock relogio) {
        return new AbrirConta(repositorio, gerador, relogio);
    }

    @Bean
    public CreditarDeposito creditarDeposito(OperacaoComTrava operacao, Clock relogio) {
        return new CreditarDeposito(operacao, relogio);
    }

    @Bean
    public Sacar sacar(OperacaoComTrava operacao, Clock relogio) {
        return new Sacar(operacao, relogio);
    }

    @Bean
    public ConsultarContas consultarContas(ContaRepository repositorio) {
        return new ConsultarContas(repositorio);
    }

    @Bean
    public LiquidarOrdem liquidarOrdem(OperacaoComTrava operacao, PublicadorDeEventosDeConta publicador,
            Clock relogio) {
        return new LiquidarOrdem(operacao, publicador, relogio);
    }

    @Bean
    public SincronizarTitular sincronizarTitular(ContaRepository repositorio, OperacaoComTrava operacao) {
        return new SincronizarTitular(repositorio, operacao);
    }
}
