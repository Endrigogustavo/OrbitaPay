package com.orbitapay.ativos.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.orbitapay.ativos.application.gateway.IAtivoGateway;
import com.orbitapay.ativos.application.gateway.IBolsaGateway;
import com.orbitapay.ativos.application.usecase.AtivoUseCase;
import com.orbitapay.ativos.service.AtivoService;

@Configuration
public class UseCaseConfig {

    @Bean
    public AtivoService ativoService() {
        return new AtivoService();
    }

    @Bean
    public AtivoUseCase ativoUseCase(AtivoService ativoService, IAtivoGateway ativoGateway, IBolsaGateway bolsaGateway) {
        return new AtivoUseCase(ativoService, ativoGateway, bolsaGateway);
    }
}
