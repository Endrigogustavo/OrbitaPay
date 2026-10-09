package com.orbitapay.carteira.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.orbitapay.carteira.application.service.CatalogoDeAtivos;
import com.orbitapay.carteira.application.service.PublicadorDeEventosDeCarteira;
import com.orbitapay.carteira.application.usecase.AtualizarCotacoes;
import com.orbitapay.carteira.application.usecase.CancelarReservaDeVenda;
import com.orbitapay.carteira.application.usecase.ConsultarCarteira;
import com.orbitapay.carteira.application.usecase.EncerrarCarteira;
import com.orbitapay.carteira.application.usecase.LiquidarCompra;
import com.orbitapay.carteira.application.usecase.LiquidarVenda;
import com.orbitapay.carteira.application.usecase.RegistrarAtivoCotado;
import com.orbitapay.carteira.application.usecase.RemoverAtivoCotado;
import com.orbitapay.carteira.application.usecase.ReservarAcoesParaVenda;
import com.orbitapay.carteira.domain.repository.AtivoCotadoRepository;
import com.orbitapay.carteira.domain.repository.CarteiraRepository;
import com.orbitapay.carteira.domain.repository.TravaDeCarteira;

@Configuration
public class CasosDeUsoConfig {

    @Bean
    public ReservarAcoesParaVenda reservarAcoesParaVenda(TravaDeCarteira trava,
            PublicadorDeEventosDeCarteira publicador) {
        return new ReservarAcoesParaVenda(trava, publicador);
    }

    @Bean
    public LiquidarCompra liquidarCompra(TravaDeCarteira trava) {
        return new LiquidarCompra(trava);
    }

    @Bean
    public LiquidarVenda liquidarVenda(TravaDeCarteira trava) {
        return new LiquidarVenda(trava);
    }

    @Bean
    public CancelarReservaDeVenda cancelarReservaDeVenda(TravaDeCarteira trava) {
        return new CancelarReservaDeVenda(trava);
    }

    @Bean
    public EncerrarCarteira encerrarCarteira(CarteiraRepository carteiras) {
        return new EncerrarCarteira(carteiras);
    }

    @Bean
    public ConsultarCarteira consultarCarteira(CarteiraRepository carteiras, AtivoCotadoRepository ativos,
            CatalogoDeAtivos catalogo) {
        return new ConsultarCarteira(carteiras, ativos, catalogo);
    }

    @Bean
    public RegistrarAtivoCotado registrarAtivoCotado(AtivoCotadoRepository ativos) {
        return new RegistrarAtivoCotado(ativos);
    }

    @Bean
    public RemoverAtivoCotado removerAtivoCotado(AtivoCotadoRepository ativos) {
        return new RemoverAtivoCotado(ativos);
    }

    @Bean
    public AtualizarCotacoes atualizarCotacoes(AtivoCotadoRepository ativos) {
        return new AtualizarCotacoes(ativos);
    }
}
