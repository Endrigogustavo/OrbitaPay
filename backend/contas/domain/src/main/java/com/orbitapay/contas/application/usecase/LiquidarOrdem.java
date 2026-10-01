package com.orbitapay.contas.application.usecase;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;

import com.orbitapay.contas.application.service.PublicadorDeEventosDeConta;
import com.orbitapay.contas.domain.event.DebitoDeCompraAprovado;
import com.orbitapay.contas.domain.event.DebitoDeCompraRecusado;
import com.orbitapay.contas.domain.exception.ContaBloqueadaException;
import com.orbitapay.contas.domain.exception.ContaNaoEncontradaException;
import com.orbitapay.contas.domain.exception.RegraDeNegocioException;
import com.orbitapay.contas.domain.model.Dinheiro;

public class LiquidarOrdem {

    public record Comando(String ordemId, String clienteId, String ticker, int quantidade, BigDecimal valorTotal) {
    }

    private final OperacaoComTrava operacao;
    private final PublicadorDeEventosDeConta publicador;
    private final Clock relogio;

    public LiquidarOrdem(OperacaoComTrava operacao, PublicadorDeEventosDeConta publicador, Clock relogio) {
        this.operacao = operacao;
        this.publicador = publicador;
        this.relogio = relogio;
    }

    public void debitarCompra(Comando comando) {
        Dinheiro valor = new Dinheiro(comando.valorTotal());
        String descricao = "Compra · " + comando.quantidade() + " " + comando.ticker();
        try {
            Dinheiro saldoRestante = operacao.executar(comando.clienteId(), conta -> {
                conta.debitarCompraDeAcoes(comando.ordemId(), valor, descricao, Instant.now(relogio));
                return conta.saldo();
            });
            publicador.publicar(new DebitoDeCompraAprovado(comando.ordemId(), comando.clienteId(), valor,
                    saldoRestante, Instant.now(relogio)));
        } catch (RegraDeNegocioException | ContaBloqueadaException | ContaNaoEncontradaException e) {
            publicador.publicar(new DebitoDeCompraRecusado(comando.ordemId(), comando.clienteId(), e.getMessage(),
                    Instant.now(relogio)));
        }
    }

    public void creditarVenda(Comando comando) {
        String descricao = "Venda · " + comando.quantidade() + " " + comando.ticker();
        operacao.executar(comando.clienteId(), conta -> conta.creditarVendaDeAcoes(comando.ordemId(),
                new Dinheiro(comando.valorTotal()), descricao, Instant.now(relogio)));
    }
}
