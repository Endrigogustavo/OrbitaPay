package com.orbitapay.relatorios.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.orbitapay.relatorios.application.usecase.GerarExtratoDeInvestimentos;
import com.orbitapay.relatorios.application.usecase.GerarRelatorioGerencial;
import com.orbitapay.relatorios.application.usecase.RegistrarFato;
import com.orbitapay.relatorios.application.usecase.RenomearCliente;
import com.orbitapay.relatorios.domain.repository.FatoRepository;
import com.orbitapay.relatorios.domain.repository.PerfilDeClienteRepository;

@Configuration
public class CasosDeUsoConfig {

    @Bean
    public Clock relogio() {
        return Clock.systemUTC();
    }

    @Bean
    public RegistrarFato registrarFato(FatoRepository fatos, PerfilDeClienteRepository perfis) {
        return new RegistrarFato(fatos, perfis);
    }

    @Bean
    public RenomearCliente renomearCliente(PerfilDeClienteRepository perfis) {
        return new RenomearCliente(perfis);
    }

    @Bean
    public GerarRelatorioGerencial gerarRelatorioGerencial(FatoRepository fatos, PerfilDeClienteRepository perfis,
            Clock relogio) {
        return new GerarRelatorioGerencial(fatos, perfis, relogio);
    }

    @Bean
    public GerarExtratoDeInvestimentos gerarExtratoDeInvestimentos(FatoRepository fatos, Clock relogio) {
        return new GerarExtratoDeInvestimentos(fatos, relogio);
    }
}
