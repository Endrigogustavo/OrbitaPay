package com.orbitapay.clientes.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.orbitapay.clientes.application.service.PublicadorDeEventosDeCliente;
import com.orbitapay.clientes.application.service.RegistroDeCredencial;
import com.orbitapay.clientes.application.usecase.AtualizarDadosDoCliente;
import com.orbitapay.clientes.application.usecase.BloquearCliente;
import com.orbitapay.clientes.application.usecase.CadastrarCliente;
import com.orbitapay.clientes.application.usecase.ConsultarClientes;
import com.orbitapay.clientes.application.usecase.DesbloquearCliente;
import com.orbitapay.clientes.application.usecase.RemoverCliente;
import com.orbitapay.clientes.domain.repository.ClienteRepository;

@Configuration
public class CasosDeUsoConfig {

    @Bean
    public CadastrarCliente cadastrarCliente(ClienteRepository repositorio, RegistroDeCredencial registroDeCredencial,
            PublicadorDeEventosDeCliente publicador) {
        return new CadastrarCliente(repositorio, registroDeCredencial, publicador);
    }

    @Bean
    public ConsultarClientes consultarClientes(ClienteRepository repositorio) {
        return new ConsultarClientes(repositorio);
    }

    @Bean
    public AtualizarDadosDoCliente atualizarDadosDoCliente(ClienteRepository repositorio,
            PublicadorDeEventosDeCliente publicador) {
        return new AtualizarDadosDoCliente(repositorio, publicador);
    }

    @Bean
    public BloquearCliente bloquearCliente(ClienteRepository repositorio,
            PublicadorDeEventosDeCliente publicador) {
        return new BloquearCliente(repositorio, publicador);
    }

    @Bean
    public DesbloquearCliente desbloquearCliente(ClienteRepository repositorio,
            PublicadorDeEventosDeCliente publicador) {
        return new DesbloquearCliente(repositorio, publicador);
    }

    @Bean
    public RemoverCliente removerCliente(ClienteRepository repositorio,
            PublicadorDeEventosDeCliente publicador) {
        return new RemoverCliente(repositorio, publicador);
    }
}
