package com.orbitapay.pagamentos.infrastructure.config;

import java.time.Clock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.orbitapay.pagamentos.application.service.ProvedorDePagamentos;
import com.orbitapay.pagamentos.application.service.PublicadorDeEventosDePagamento;
import com.orbitapay.pagamentos.application.usecase.ConciliarPagamentos;
import com.orbitapay.pagamentos.application.usecase.ConsultarPagamentos;
import com.orbitapay.pagamentos.application.usecase.SolicitarPagamento;
import com.orbitapay.pagamentos.domain.repository.PagamentoRepository;

@Configuration
@EnableScheduling
public class CasosDeUsoConfig {

    @Bean
    public Clock relogio() {
        return Clock.systemUTC();
    }

    @Bean
    public SolicitarPagamento solicitarPagamento(PagamentoRepository repositorio, ProvedorDePagamentos provedores,
            Clock relogio) {
        return new SolicitarPagamento(repositorio, provedores, relogio);
    }

    @Bean
    public ConsultarPagamentos consultarPagamentos(PagamentoRepository repositorio) {
        return new ConsultarPagamentos(repositorio);
    }

    @Bean
    public ConciliarPagamentos conciliarPagamentos(PagamentoRepository repositorio, ProvedorDePagamentos provedores,
            PublicadorDeEventosDePagamento publicador, Clock relogio,
            @Value("${orbita.conciliacao.lote}") int lote) {
        return new ConciliarPagamentos(repositorio, provedores, publicador, relogio, lote);
    }
}
