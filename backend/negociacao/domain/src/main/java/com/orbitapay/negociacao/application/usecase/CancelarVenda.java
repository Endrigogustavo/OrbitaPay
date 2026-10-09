package com.orbitapay.negociacao.application.usecase;

import java.time.Instant;
import java.util.Optional;

import com.orbitapay.negociacao.application.service.PublicadorDeEventosDeOrdem;
import com.orbitapay.negociacao.domain.event.OrdemRejeitada;
import com.orbitapay.negociacao.domain.model.Ordem;
import com.orbitapay.negociacao.domain.repository.OrdemRepository;

public class CancelarVenda {

    private final OrdemRepository ordens;
    private final PublicadorDeEventosDeOrdem publicador;

    public CancelarVenda(OrdemRepository ordens, PublicadorDeEventosDeOrdem publicador) {
        this.ordens = ordens;
        this.publicador = publicador;
    }

    public void executar(String ordemId, String motivo) {
        Optional<Ordem> encontrada = ordens.buscar(ordemId);
        if (encontrada.isEmpty() || !encontrada.get().pendente()) {
            return;
        }
        Ordem ordem = encontrada.get();

        ordem.rejeitar(motivo, Instant.now());
        ordens.salvar(ordem);
        publicador.publicar(new OrdemRejeitada(ordem, Instant.now()));
    }
}
