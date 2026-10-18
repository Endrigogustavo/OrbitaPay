package com.orbitapay.auth.infrastructure.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.orbitapay.auth.application.gateway.IAssinaturaGateway;
import com.orbitapay.auth.application.gateway.IClienteGateway;
import com.orbitapay.auth.application.gateway.ICodificadorDePinGateway;
import com.orbitapay.auth.application.gateway.ICredencialGateway;
import com.orbitapay.auth.application.gateway.IFuncionarioGateway;
import com.orbitapay.auth.application.gateway.ISessaoGateway;
import com.orbitapay.auth.application.usecase.AssinaturaUseCase;
import com.orbitapay.auth.application.usecase.CredencialUseCase;
import com.orbitapay.auth.application.usecase.SessaoUseCase;
import com.orbitapay.auth.service.CredencialService;

@Configuration
public class UseCaseConfig {

    @Bean
    public CredencialService credencialService() {
        return new CredencialService();
    }

    @Bean
    public CredencialUseCase credencialUseCase(CredencialService credencialService,
            ICredencialGateway credencialGateway, IFuncionarioGateway funcionarioGateway, ISessaoGateway sessaoGateway,
            IClienteGateway clienteGateway, ICodificadorDePinGateway codificadorDePinGateway) {
        return new CredencialUseCase(credencialService, credencialGateway, funcionarioGateway, sessaoGateway,
                clienteGateway, codificadorDePinGateway);
    }

    @Bean
    public SessaoUseCase sessaoUseCase(CredencialService credencialService, CredencialUseCase credencialUseCase,
            ICredencialGateway credencialGateway, IFuncionarioGateway funcionarioGateway, ISessaoGateway sessaoGateway,
            ICodificadorDePinGateway codificadorDePinGateway,
            @Value("${orbita.sessao.duracao}") Duration duracaoDaSessao) {
        return new SessaoUseCase(credencialService, credencialUseCase, credencialGateway, funcionarioGateway,
                sessaoGateway, codificadorDePinGateway, duracaoDaSessao);
    }

    @Bean
    public AssinaturaUseCase assinaturaUseCase(CredencialUseCase credencialUseCase, SessaoUseCase sessaoUseCase,
            IAssinaturaGateway assinaturaGateway, @Value("${orbita.assinatura.duracao}") Duration duracaoDaAssinatura) {
        return new AssinaturaUseCase(credencialUseCase, sessaoUseCase, assinaturaGateway, duracaoDaAssinatura);
    }
}
