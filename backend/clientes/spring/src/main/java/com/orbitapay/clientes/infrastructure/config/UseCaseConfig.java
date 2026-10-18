package com.orbitapay.clientes.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.orbitapay.clientes.application.gateway.IClienteGateway;
import com.orbitapay.clientes.application.gateway.IContaGateway;
import com.orbitapay.clientes.application.gateway.ICredencialGateway;
import com.orbitapay.clientes.application.gateway.IEventoGateway;
import com.orbitapay.clientes.application.gateway.ITransacaoGateway;
import com.orbitapay.clientes.application.usecase.ClienteUseCase;
import com.orbitapay.clientes.application.usecase.ContaUseCase;
import com.orbitapay.clientes.service.ClienteService;
import com.orbitapay.clientes.service.ContaService;

@Configuration
public class UseCaseConfig {

    @Bean
    public ClienteService clienteService() {
        return new ClienteService();
    }

    @Bean
    public ContaService contaService() {
        return new ContaService();
    }

    @Bean
    public ContaUseCase contaUseCase(ContaService contaService, IContaGateway contaGateway,
            IClienteGateway clienteGateway, ITransacaoGateway transacaoGateway) {
        return new ContaUseCase(contaService, contaGateway, clienteGateway, transacaoGateway);
    }

    @Bean
    public ClienteUseCase clienteUseCase(ClienteService clienteService, IClienteGateway clienteGateway,
            ICredencialGateway credencialGateway, IEventoGateway eventoGateway, ITransacaoGateway transacaoGateway,
            ContaUseCase contaUseCase) {
        return new ClienteUseCase(clienteService, clienteGateway, credencialGateway, eventoGateway, transacaoGateway,
                contaUseCase);
    }
}
