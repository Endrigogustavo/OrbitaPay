package com.orbitapay.contas.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.orbitapay.contas.application.port.in.AbrirContaUseCase;
import com.orbitapay.contas.application.port.in.ConsultarContasUseCase;
import com.orbitapay.contas.application.port.in.DepositarUseCase;
import com.orbitapay.contas.application.port.in.LiquidarOrdemUseCase;
import com.orbitapay.contas.application.port.in.SacarUseCase;
import com.orbitapay.contas.application.port.in.SincronizarTitularUseCase;
import com.orbitapay.contas.application.port.out.ContaRepository;
import com.orbitapay.contas.application.port.out.GeradorDeNumeroDeConta;
import com.orbitapay.contas.application.port.out.PublicadorDeEventosDeConta;
import com.orbitapay.contas.application.usecase.AbrirConta;
import com.orbitapay.contas.application.usecase.ConsultarContas;
import com.orbitapay.contas.application.usecase.Depositar;
import com.orbitapay.contas.application.usecase.LiquidarOrdem;
import com.orbitapay.contas.application.usecase.OperacaoComTrava;
import com.orbitapay.contas.application.usecase.Sacar;
import com.orbitapay.contas.application.usecase.SincronizarTitular;

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
    public AbrirContaUseCase abrirConta(ContaRepository repositorio, GeradorDeNumeroDeConta gerador, Clock relogio) {
        return new AbrirConta(repositorio, gerador, relogio);
    }

    @Bean
    public DepositarUseCase depositar(OperacaoComTrava operacao, Clock relogio) {
        return new Depositar(operacao, relogio);
    }

    @Bean
    public SacarUseCase sacar(OperacaoComTrava operacao, Clock relogio) {
        return new Sacar(operacao, relogio);
    }

    @Bean
    public ConsultarContasUseCase consultarContas(ContaRepository repositorio) {
        return new ConsultarContas(repositorio);
    }

    @Bean
    public LiquidarOrdemUseCase liquidarOrdem(OperacaoComTrava operacao, PublicadorDeEventosDeConta publicador,
            Clock relogio) {
        return new LiquidarOrdem(operacao, publicador, relogio);
    }

    @Bean
    public SincronizarTitularUseCase sincronizarTitular(ContaRepository repositorio, OperacaoComTrava operacao) {
        return new SincronizarTitular(repositorio, operacao);
    }
}
