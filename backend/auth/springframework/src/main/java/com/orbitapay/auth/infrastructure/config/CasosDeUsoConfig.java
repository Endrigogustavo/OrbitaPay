package com.orbitapay.auth.infrastructure.config;

import java.time.Clock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.orbitapay.auth.application.service.CodificadorDePin;
import com.orbitapay.auth.application.service.EmissorDeToken;
import com.orbitapay.auth.application.service.PublicadorDeEventosDeCredencial;
import com.orbitapay.auth.application.usecase.AlterarPin;
import com.orbitapay.auth.application.usecase.AssinarOperacao;
import com.orbitapay.auth.application.usecase.AutenticarCliente;
import com.orbitapay.auth.application.usecase.AutenticarGerente;
import com.orbitapay.auth.application.usecase.ConferenciaDePin;
import com.orbitapay.auth.application.usecase.ConsultarCredencial;
import com.orbitapay.auth.application.usecase.RegistrarCredencial;
import com.orbitapay.auth.application.usecase.SincronizarCliente;
import com.orbitapay.auth.domain.repository.CredencialRepository;

@Configuration
public class CasosDeUsoConfig {

    @Bean
    public Clock relogio() {
        return Clock.systemUTC();
    }

    @Bean
    public ConferenciaDePin conferenciaDePin(CredencialRepository repositorio, CodificadorDePin codificador,
            PublicadorDeEventosDeCredencial publicador, Clock relogio) {
        return new ConferenciaDePin(repositorio, codificador, publicador, relogio);
    }

    @Bean
    public RegistrarCredencial registrarCredencial(CredencialRepository repositorio, CodificadorDePin codificador,
            Clock relogio) {
        return new RegistrarCredencial(repositorio, codificador, relogio);
    }

    @Bean
    public AutenticarCliente autenticarCliente(CredencialRepository repositorio, ConferenciaDePin conferencia,
            EmissorDeToken emissor) {
        return new AutenticarCliente(repositorio, conferencia, emissor);
    }

    @Bean
    public AutenticarGerente autenticarGerente(@Value("${orbita.gerente.codigo}") String codigo,
            EmissorDeToken emissor) {
        return new AutenticarGerente(codigo, emissor);
    }

    @Bean
    public AssinarOperacao assinarOperacao(CredencialRepository repositorio, ConferenciaDePin conferencia,
            EmissorDeToken emissor) {
        return new AssinarOperacao(repositorio, conferencia, emissor);
    }

    @Bean
    public AlterarPin alterarPin(CredencialRepository repositorio, CodificadorDePin codificador) {
        return new AlterarPin(repositorio, codificador);
    }

    @Bean
    public ConsultarCredencial consultarCredencial(CredencialRepository repositorio) {
        return new ConsultarCredencial(repositorio);
    }

    @Bean
    public SincronizarCliente sincronizarCliente(CredencialRepository repositorio) {
        return new SincronizarCliente(repositorio);
    }
}
