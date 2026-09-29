package com.orbitapay.carteira.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.orbitapay.carteira.application.port.in.ConsultarCarteiraUseCase;
import com.orbitapay.carteira.application.port.in.MovimentarCarteiraUseCase;
import com.orbitapay.carteira.application.port.in.SincronizarCotacoesUseCase;
import com.orbitapay.carteira.application.port.out.AtivoCotadoRepository;
import com.orbitapay.carteira.application.port.out.CarteiraRepository;
import com.orbitapay.carteira.application.port.out.CatalogoDeAtivos;
import com.orbitapay.carteira.application.port.out.PublicadorDeEventosDeCarteira;
import com.orbitapay.carteira.application.usecase.ConsultarCarteira;
import com.orbitapay.carteira.application.usecase.MovimentarCarteira;
import com.orbitapay.carteira.application.usecase.OperacaoComTrava;
import com.orbitapay.carteira.application.usecase.SincronizarCotacoes;

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
    public MovimentarCarteiraUseCase movimentarCarteira(CarteiraRepository carteiras, OperacaoComTrava operacao,
            PublicadorDeEventosDeCarteira publicador, Clock relogio) {
        return new MovimentarCarteira(carteiras, operacao, publicador, relogio);
    }

    @Bean
    public ConsultarCarteiraUseCase consultarCarteira(CarteiraRepository carteiras, AtivoCotadoRepository ativos,
            CatalogoDeAtivos catalogo) {
        return new ConsultarCarteira(carteiras, ativos, catalogo);
    }

    @Bean
    public SincronizarCotacoesUseCase sincronizarCotacoes(AtivoCotadoRepository ativos) {
        return new SincronizarCotacoes(ativos);
    }
}
