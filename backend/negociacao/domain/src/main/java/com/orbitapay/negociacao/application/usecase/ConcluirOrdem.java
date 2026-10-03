package com.orbitapay.negociacao.application.usecase;

import java.time.Instant;
import java.util.Optional;

import com.orbitapay.negociacao.application.service.PublicadorDeEventosDeOrdem;
import com.orbitapay.negociacao.domain.event.OrdemExecutada;
import com.orbitapay.negociacao.domain.event.OrdemRejeitada;
import com.orbitapay.negociacao.domain.model.Ordem;
import com.orbitapay.negociacao.domain.repository.AtivoNegociavelRepository;
import com.orbitapay.negociacao.domain.repository.AtivoTravado;
import com.orbitapay.negociacao.domain.repository.OrdemRepository;

public class ConcluirOrdem {

    private final OrdemRepository ordens;
    private final AtivoNegociavelRepository ativos;
    private final PublicadorDeEventosDeOrdem publicador;

    public ConcluirOrdem(OrdemRepository ordens, AtivoNegociavelRepository ativos,
            PublicadorDeEventosDeOrdem publicador) {
        this.ordens = ordens;
        this.ativos = ativos;
        this.publicador = publicador;
    }

    public void pagamentoAprovado(String ordemId) {
        Ordem ordem = buscarPendente(ordemId);
        if (ordem == null) {
            return;
        }
        AtivoTravado travado = ativos.travar(ordem.ticker());
        try {
            travado.ativo().confirmarCompra(ordem.quantidade());
            ativos.salvarELiberar(travado);
        } catch (RuntimeException erro) {
            ativos.liberar(travado);
            throw erro;
        }
        executar(ordem);
    }

    public void pagamentoRecusado(String ordemId, String motivo) {
        Ordem ordem = buscarPendente(ordemId);
        if (ordem == null) {
            return;
        }
        AtivoTravado travado = ativos.travar(ordem.ticker());
        try {
            travado.ativo().cancelarReserva(ordem.quantidade());
            ativos.salvarELiberar(travado);
        } catch (RuntimeException erro) {
            ativos.liberar(travado);
            throw erro;
        }
        rejeitar(ordem, motivo);
    }

    public void acoesReservadas(String ordemId) {
        Ordem ordem = buscarPendente(ordemId);
        if (ordem == null) {
            return;
        }
        AtivoTravado travado = ativos.travar(ordem.ticker());
        try {
            travado.ativo().receberVenda(ordem.quantidade());
            ativos.salvarELiberar(travado);
        } catch (RuntimeException erro) {
            ativos.liberar(travado);
            throw erro;
        }
        executar(ordem);
    }

    public void acoesIndisponiveis(String ordemId, String motivo) {
        Ordem ordem = buscarPendente(ordemId);
        if (ordem == null) {
            return;
        }
        rejeitar(ordem, motivo);
    }

    private Ordem buscarPendente(String ordemId) {
        Optional<Ordem> ordem = ordens.buscar(ordemId);
        if (ordem.isEmpty() || !ordem.get().pendente()) {
            return null;
        }
        return ordem.get();
    }

    private void executar(Ordem ordem) {
        ordem.executar(Instant.now());
        ordens.salvar(ordem);
        publicador.publicar(new OrdemExecutada(ordem, Instant.now()));
    }

    private void rejeitar(Ordem ordem, String motivo) {
        ordem.rejeitar(motivo, Instant.now());
        ordens.salvar(ordem);
        publicador.publicar(new OrdemRejeitada(ordem, Instant.now()));
    }
}
