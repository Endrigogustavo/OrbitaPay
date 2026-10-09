package com.orbitapay.contas.application.usecase;

import java.time.Instant;

import com.orbitapay.contas.application.dto.OrdemRecebida;
import com.orbitapay.contas.application.service.PublicadorDeEventosDeConta;
import com.orbitapay.contas.domain.event.DebitoDeCompraAprovado;
import com.orbitapay.contas.domain.event.DebitoDeCompraRecusado;
import com.orbitapay.contas.domain.exception.ContaBloqueadaException;
import com.orbitapay.contas.domain.exception.ContaNaoEncontradaException;
import com.orbitapay.contas.domain.exception.RegraDeNegocioException;
import com.orbitapay.contas.domain.model.Conta;
import com.orbitapay.contas.domain.model.Dinheiro;
import com.orbitapay.contas.domain.repository.ContaTravada;
import com.orbitapay.contas.domain.repository.TravaDeConta;

public class DebitarCompra {

    private final TravaDeConta trava;
    private final PublicadorDeEventosDeConta publicador;

    public DebitarCompra(TravaDeConta trava, PublicadorDeEventosDeConta publicador) {
        this.trava = trava;
        this.publicador = publicador;
    }

    public void executar(OrdemRecebida ordem) {
        Dinheiro valor = new Dinheiro(ordem.valorTotal());
        String descricao = "Compra · " + ordem.quantidade() + " " + ordem.ticker();
        try {
            Dinheiro saldoRestante = debitar(ordem, valor, descricao);
            publicador.publicar(new DebitoDeCompraAprovado(ordem.ordemId(), ordem.clienteId(), valor, saldoRestante,
                    Instant.now()));
        } catch (RegraDeNegocioException | ContaBloqueadaException | ContaNaoEncontradaException erro) {
            publicador.publicar(new DebitoDeCompraRecusado(ordem.ordemId(), ordem.clienteId(), erro.getMessage(),
                    Instant.now()));
        }
    }

    private Dinheiro debitar(OrdemRecebida ordem, Dinheiro valor, String descricao) {
        ContaTravada travada = trava.travar(ordem.clienteId());
        try {
            Conta conta = travada.conta();
            conta.debitarCompraDeAcoes(ordem.ordemId(), valor, descricao, Instant.now());
            trava.salvarELiberar(travada);
            return conta.saldo();
        } catch (RuntimeException erro) {
            trava.liberar(travada);
            throw erro;
        }
    }
}
