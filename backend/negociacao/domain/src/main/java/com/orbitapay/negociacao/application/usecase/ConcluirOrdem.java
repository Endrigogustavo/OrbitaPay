package com.orbitapay.negociacao.application.usecase;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;

import com.orbitapay.negociacao.application.service.PublicadorDeEventosDeOrdem;
import com.orbitapay.negociacao.domain.event.OrdemExecutada;
import com.orbitapay.negociacao.domain.event.OrdemRejeitada;
import com.orbitapay.negociacao.domain.model.AtivoNegociavel;
import com.orbitapay.negociacao.domain.model.Ordem;
import com.orbitapay.negociacao.domain.repository.OrdemRepository;

public class ConcluirOrdem {

    private final OrdemRepository ordens;
    private final OperacaoComTrava operacao;
    private final PublicadorDeEventosDeOrdem publicador;
    private final Clock relogio;

    public ConcluirOrdem(OrdemRepository ordens, OperacaoComTrava operacao, PublicadorDeEventosDeOrdem publicador,
            Clock relogio) {
        this.ordens = ordens;
        this.operacao = operacao;
        this.publicador = publicador;
        this.relogio = relogio;
    }

    public void pagamentoAprovado(String ordemId) {
        pendente(ordemId).ifPresent(ordem -> {
            ajustarOferta(ordem, ativo -> ativo.confirmarCompra(ordem.quantidade()));
            executar(ordem);
        });
    }

    public void pagamentoRecusado(String ordemId, String motivo) {
        pendente(ordemId).ifPresent(ordem -> {
            ajustarOferta(ordem, ativo -> ativo.cancelarReserva(ordem.quantidade()));
            rejeitar(ordem, motivo);
        });
    }

    public void acoesReservadas(String ordemId) {
        pendente(ordemId).ifPresent(ordem -> {
            ajustarOferta(ordem, ativo -> ativo.receberVenda(ordem.quantidade()));
            executar(ordem);
        });
    }

    public void acoesIndisponiveis(String ordemId, String motivo) {
        pendente(ordemId).ifPresent(ordem -> rejeitar(ordem, motivo));
    }

    private Optional<Ordem> pendente(String ordemId) {
        return ordens.buscar(ordemId).filter(Ordem::pendente);
    }

    private void ajustarOferta(Ordem ordem, Consumer<AtivoNegociavel> ajuste) {
        operacao.executar(ordem.ticker(), ativo -> {
            ajuste.accept(ativo);
            return ativo;
        });
    }

    private void executar(Ordem ordem) {
        ordem.executar(Instant.now(relogio));
        ordens.salvar(ordem);
        publicador.publicar(new OrdemExecutada(ordem, Instant.now(relogio)));
    }

    private void rejeitar(Ordem ordem, String motivo) {
        ordem.rejeitar(motivo, Instant.now(relogio));
        ordens.salvar(ordem);
        publicador.publicar(new OrdemRejeitada(ordem, Instant.now(relogio)));
    }
}
