package com.orbitapay.carteira.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.orbitapay.carteira.application.service.CatalogoDeAtivos;
import com.orbitapay.carteira.application.service.PublicadorDeEventosDeCarteira;
import com.orbitapay.carteira.application.usecase.ConsultarCarteira;
import com.orbitapay.carteira.application.usecase.MovimentarCarteira;
import com.orbitapay.carteira.application.usecase.OperacaoComTrava;
import com.orbitapay.carteira.application.usecase.SincronizarCotacoes;
import com.orbitapay.carteira.domain.repository.AtivoCotadoRepository;
import com.orbitapay.carteira.domain.repository.CarteiraRepository;

@Configuration
public class CasosDeUsoConfig {

    @Bean
    public Clock relogio() {
        return Clock.systemUTC();
    }

    @Bean
    public OperacaoComTrava operacaoComTrava(CarteiraRepository carteiras) {
        return new OperacaoComTrava(carteiras);
    }

    @Bean
    public MovimentarCarteira movimentarCarteira(CarteiraRepository carteiras, OperacaoComTrava operacao,
            PublicadorDeEventosDeCarteira publicador, Clock relogio) {
        return new MovimentarCarteira(carteiras, operacao, publicador, relogio);
    }

    @Bean
    public ConsultarCarteira consultarCarteira(CarteiraRepository carteiras, AtivoCotadoRepository ativos,
            CatalogoDeAtivos catalogo) {
        return new ConsultarCarteira(carteiras, ativos, catalogo);
    }

    @Bean
    public SincronizarCotacoes sincronizarCotacoes(AtivoCotadoRepository ativos) {
        return new SincronizarCotacoes(ativos);
    }
}
