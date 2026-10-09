package com.orbitapay.contas.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.orbitapay.contas.application.service.GeradorDeNumeroDeConta;
import com.orbitapay.contas.application.service.PublicadorDeEventosDeConta;
import com.orbitapay.contas.application.usecase.AbrirConta;
import com.orbitapay.contas.application.usecase.AlterarSituacaoDoTitular;
import com.orbitapay.contas.application.usecase.AtualizarNomeDoTitular;
import com.orbitapay.contas.application.usecase.ConsultarContas;
import com.orbitapay.contas.application.usecase.CreditarDeposito;
import com.orbitapay.contas.application.usecase.CreditarVenda;
import com.orbitapay.contas.application.usecase.DebitarCompra;
import com.orbitapay.contas.application.usecase.EncerrarConta;
import com.orbitapay.contas.application.usecase.Sacar;
import com.orbitapay.contas.domain.repository.ContaRepository;
import com.orbitapay.contas.domain.repository.TravaDeConta;

@Configuration
public class CasosDeUsoConfig {

    @Bean
    public AbrirConta abrirConta(ContaRepository contas, GeradorDeNumeroDeConta gerador) {
        return new AbrirConta(contas, gerador);
    }

    @Bean
    public CreditarDeposito creditarDeposito(TravaDeConta trava) {
        return new CreditarDeposito(trava);
    }

    @Bean
    public Sacar sacar(TravaDeConta trava) {
        return new Sacar(trava);
    }

    @Bean
    public ConsultarContas consultarContas(ContaRepository contas) {
        return new ConsultarContas(contas);
    }

    @Bean
    public DebitarCompra debitarCompra(TravaDeConta trava, PublicadorDeEventosDeConta publicador) {
        return new DebitarCompra(trava, publicador);
    }

    @Bean
    public CreditarVenda creditarVenda(TravaDeConta trava) {
        return new CreditarVenda(trava);
    }

    @Bean
    public AtualizarNomeDoTitular atualizarNomeDoTitular(ContaRepository contas, TravaDeConta trava) {
        return new AtualizarNomeDoTitular(contas, trava);
    }

    @Bean
    public AlterarSituacaoDoTitular alterarSituacaoDoTitular(ContaRepository contas, TravaDeConta trava) {
        return new AlterarSituacaoDoTitular(contas, trava);
    }

    @Bean
    public EncerrarConta encerrarConta(ContaRepository contas) {
        return new EncerrarConta(contas);
    }
}
