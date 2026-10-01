package com.orbitapay.carteira.application.usecase;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;

import com.orbitapay.carteira.application.service.PublicadorDeEventosDeCarteira;
import com.orbitapay.carteira.domain.event.AcoesInsuficientes;
import com.orbitapay.carteira.domain.event.AcoesReservadas;
import com.orbitapay.carteira.domain.exception.AcoesInsuficientesException;
import com.orbitapay.carteira.domain.repository.CarteiraRepository;

public class MovimentarCarteira {

    public record Comando(String ordemId, String clienteId, String ticker, int quantidade, BigDecimal precoUnitario) {
    }

    private final CarteiraRepository repositorio;
    private final OperacaoComTrava operacao;
    private final PublicadorDeEventosDeCarteira publicador;
    private final Clock relogio;

    public MovimentarCarteira(CarteiraRepository repositorio, OperacaoComTrava operacao,
            PublicadorDeEventosDeCarteira publicador, Clock relogio) {
        this.repositorio = repositorio;
        this.operacao = operacao;
        this.publicador = publicador;
        this.relogio = relogio;
    }

    public void reservarParaVenda(Comando comando) {
        try {
            operacao.executar(comando.clienteId(), carteira -> carteira.reservarParaVenda(comando.ordemId(),
                    comando.ticker(), comando.quantidade()));
            publicador.publicar(new AcoesReservadas(comando.ordemId(), comando.clienteId(), comando.ticker(),
                    comando.quantidade(), Instant.now(relogio)));
        } catch (AcoesInsuficientesException e) {
            publicador.publicar(new AcoesInsuficientes(comando.ordemId(), comando.clienteId(), comando.ticker(),
                    e.getMessage(), Instant.now(relogio)));
        }
    }

    public void liquidarCompra(Comando comando) {
        operacao.executar(comando.clienteId(), carteira -> carteira.registrarCompra(comando.ordemId(),
                comando.ticker(), comando.quantidade(), comando.precoUnitario()));
    }

    public void liquidarVenda(Comando comando) {
        operacao.executar(comando.clienteId(),
                carteira -> carteira.liquidarVenda(comando.ordemId(), comando.ticker()));
    }

    public void cancelarVenda(Comando comando) {
        operacao.executar(comando.clienteId(),
                carteira -> carteira.cancelarReserva(comando.ordemId(), comando.ticker()));
    }

    public void encerrar(String clienteId) {
        repositorio.removerPorCliente(clienteId);
    }
}
