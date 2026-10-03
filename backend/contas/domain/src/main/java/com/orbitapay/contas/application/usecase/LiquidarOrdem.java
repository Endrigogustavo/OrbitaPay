package com.orbitapay.contas.application.usecase;

import java.math.BigDecimal;
import java.time.Instant;

import com.orbitapay.contas.application.service.PublicadorDeEventosDeConta;
import com.orbitapay.contas.domain.event.DebitoDeCompraAprovado;
import com.orbitapay.contas.domain.event.DebitoDeCompraRecusado;
import com.orbitapay.contas.domain.exception.ContaBloqueadaException;
import com.orbitapay.contas.domain.exception.ContaNaoEncontradaException;
import com.orbitapay.contas.domain.exception.RegraDeNegocioException;
import com.orbitapay.contas.domain.model.Conta;
import com.orbitapay.contas.domain.model.Dinheiro;
import com.orbitapay.contas.domain.repository.ContaRepository;
import com.orbitapay.contas.domain.repository.ContaTravada;

public class LiquidarOrdem {

    public record Comando(String ordemId, String clienteId, String ticker, int quantidade, BigDecimal valorTotal) {
    }

    private final ContaRepository repositorio;
    private final PublicadorDeEventosDeConta publicador;

    public LiquidarOrdem(ContaRepository repositorio, PublicadorDeEventosDeConta publicador) {
        this.repositorio = repositorio;
        this.publicador = publicador;
    }

    public void debitarCompra(Comando comando) {
        Dinheiro valor = new Dinheiro(comando.valorTotal());
        String descricao = "Compra · " + comando.quantidade() + " " + comando.ticker();
        try {
            Dinheiro saldoRestante = debitar(comando.clienteId(), comando.ordemId(), valor, descricao);
            publicador.publicar(new DebitoDeCompraAprovado(comando.ordemId(), comando.clienteId(), valor,
                    saldoRestante, Instant.now()));
        } catch (RegraDeNegocioException | ContaBloqueadaException | ContaNaoEncontradaException erro) {
            publicador.publicar(new DebitoDeCompraRecusado(comando.ordemId(), comando.clienteId(), erro.getMessage(),
                    Instant.now()));
        }
    }

    public void creditarVenda(Comando comando) {
        String descricao = "Venda · " + comando.quantidade() + " " + comando.ticker();
        ContaTravada travada = repositorio.travarPorCliente(comando.clienteId());
        try {
            travada.conta().creditarVendaDeAcoes(comando.ordemId(), new Dinheiro(comando.valorTotal()), descricao,
                    Instant.now());
            repositorio.salvarELiberar(travada);
        } catch (RuntimeException erro) {
            repositorio.liberar(travada);
            throw erro;
        }
    }

    private Dinheiro debitar(String clienteId, String ordemId, Dinheiro valor, String descricao) {
        ContaTravada travada = repositorio.travarPorCliente(clienteId);
        try {
            Conta conta = travada.conta();
            conta.debitarCompraDeAcoes(ordemId, valor, descricao, Instant.now());
            repositorio.salvarELiberar(travada);
            return conta.saldo();
        } catch (RuntimeException erro) {
            repositorio.liberar(travada);
            throw erro;
        }
    }
}
