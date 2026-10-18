package com.orbitapay.pagamentos.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.orbitapay.pagamentos.application.gateway.IEventoGateway;
import com.orbitapay.pagamentos.application.gateway.IPagamentoGateway;
import com.orbitapay.pagamentos.application.gateway.IProvedorDePagamentoGateway;
import com.orbitapay.pagamentos.application.usecase.PagamentoUseCase;
import com.orbitapay.pagamentos.service.PagamentoService;

@Configuration
public class UseCaseConfig {

    @Bean
    public PagamentoService pagamentoService() {
        return new PagamentoService();
    }

    @Bean
    public PagamentoUseCase pagamentoUseCase(PagamentoService pagamentoService, IPagamentoGateway pagamentoGateway,
            IProvedorDePagamentoGateway provedorDePagamentoGateway, IEventoGateway eventoGateway) {
        return new PagamentoUseCase(pagamentoService, pagamentoGateway, provedorDePagamentoGateway, eventoGateway);
    }
}
