package com.orbitapay.clientes.infrastructure.config;

import java.time.Clock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.orbitapay.clientes.application.port.in.AlterarPinUseCase;
import com.orbitapay.clientes.application.port.in.AssinarOperacaoUseCase;
import com.orbitapay.clientes.application.port.in.AtualizarDadosDoClienteUseCase;
import com.orbitapay.clientes.application.port.in.AutenticarClienteUseCase;
import com.orbitapay.clientes.application.port.in.AutenticarGerenteUseCase;
import com.orbitapay.clientes.application.port.in.BloquearClienteUseCase;
import com.orbitapay.clientes.application.port.in.CadastrarClienteUseCase;
import com.orbitapay.clientes.application.port.in.ConsultarClientesUseCase;
import com.orbitapay.clientes.application.port.in.DesbloquearClienteUseCase;
import com.orbitapay.clientes.application.port.in.RemoverClienteUseCase;
import com.orbitapay.clientes.application.port.out.ClienteRepository;
import com.orbitapay.clientes.application.port.out.CodificadorDePin;
import com.orbitapay.clientes.application.port.out.EmissorDeCredencial;
import com.orbitapay.clientes.application.port.out.PublicadorDeEventosDeCliente;
import com.orbitapay.clientes.application.usecase.AlterarPin;
import com.orbitapay.clientes.application.usecase.AssinarOperacao;
import com.orbitapay.clientes.application.usecase.AtualizarDadosDoCliente;
import com.orbitapay.clientes.application.usecase.AutenticarCliente;
import com.orbitapay.clientes.application.usecase.AutenticarGerente;
import com.orbitapay.clientes.application.usecase.BloquearCliente;
import com.orbitapay.clientes.application.usecase.CadastrarCliente;
import com.orbitapay.clientes.application.usecase.ConferenciaDePin;
import com.orbitapay.clientes.application.usecase.ConsultarClientes;
import com.orbitapay.clientes.application.usecase.DesbloquearCliente;
import com.orbitapay.clientes.application.usecase.RemoverCliente;

@Configuration
public class CasosDeUsoConfig {

    @Bean
    public Clock relogio() {
        return Clock.systemUTC();
    }

    @Bean
    public ConferenciaDePin conferenciaDePin(ClienteRepository repositorio, CodificadorDePin codificador,
            PublicadorDeEventosDeCliente publicador, Clock relogio) {
        return new ConferenciaDePin(repositorio, codificador, publicador, relogio);
    }

    @Bean
    public CadastrarClienteUseCase cadastrarCliente(ClienteRepository repositorio, CodificadorDePin codificador,
            PublicadorDeEventosDeCliente publicador, Clock relogio) {
        return new CadastrarCliente(repositorio, codificador, publicador, relogio);
    }

    @Bean
    public AutenticarClienteUseCase autenticarCliente(ClienteRepository repositorio, ConferenciaDePin conferencia,
            EmissorDeCredencial emissor) {
        return new AutenticarCliente(repositorio, conferencia, emissor);
    }

    @Bean
    public AutenticarGerenteUseCase autenticarGerente(@Value("${orbita.gerente.codigo}") String codigo,
            EmissorDeCredencial emissor) {
        return new AutenticarGerente(codigo, emissor);
    }

    @Bean
    public AssinarOperacaoUseCase assinarOperacao(ClienteRepository repositorio, ConferenciaDePin conferencia,
            EmissorDeCredencial emissor) {
        return new AssinarOperacao(repositorio, conferencia, emissor);
    }

    @Bean
    public ConsultarClientesUseCase consultarClientes(ClienteRepository repositorio) {
        return new ConsultarClientes(repositorio);
    }

    @Bean
    public AtualizarDadosDoClienteUseCase atualizarDadosDoCliente(ClienteRepository repositorio,
            PublicadorDeEventosDeCliente publicador, Clock relogio) {
        return new AtualizarDadosDoCliente(repositorio, publicador, relogio);
    }

    @Bean
    public AlterarPinUseCase alterarPin(ClienteRepository repositorio, CodificadorDePin codificador) {
        return new AlterarPin(repositorio, codificador);
    }

    @Bean
    public BloquearClienteUseCase bloquearCliente(ClienteRepository repositorio,
            PublicadorDeEventosDeCliente publicador, Clock relogio) {
        return new BloquearCliente(repositorio, publicador, relogio);
    }

    @Bean
    public DesbloquearClienteUseCase desbloquearCliente(ClienteRepository repositorio, ConferenciaDePin conferencia,
            PublicadorDeEventosDeCliente publicador, Clock relogio) {
        return new DesbloquearCliente(repositorio, conferencia, publicador, relogio);
    }

    @Bean
    public RemoverClienteUseCase removerCliente(ClienteRepository repositorio,
            PublicadorDeEventosDeCliente publicador, Clock relogio) {
        return new RemoverCliente(repositorio, publicador, relogio);
    }
}
