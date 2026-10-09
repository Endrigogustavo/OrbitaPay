package com.orbitapay.negociacao.application.usecase;

import java.time.Instant;
import java.util.Optional;

import com.orbitapay.negociacao.application.service.PublicadorDeEventosDeOrdem;
import com.orbitapay.negociacao.domain.event.OrdemExecutada;
import com.orbitapay.negociacao.domain.model.Ordem;
import com.orbitapay.negociacao.domain.repository.AtivoTravado;
import com.orbitapay.negociacao.domain.repository.OrdemRepository;
import com.orbitapay.negociacao.domain.repository.TravaDeAtivo;

public class ConfirmarCompra {

    private final OrdemRepository ordens;
    private final TravaDeAtivo trava;
    private final PublicadorDeEventosDeOrdem publicador;

    public ConfirmarCompra(OrdemRepository ordens, TravaDeAtivo trava, PublicadorDeEventosDeOrdem publicador) {
        this.ordens = ordens;
        this.trava = trava;
        this.publicador = publicador;
    }

    public void executar(String ordemId) {
        Optional<Ordem> encontrada = ordens.buscar(ordemId);
        if (encontrada.isEmpty() || !encontrada.get().pendente()) {
            return;
        }
        Ordem ordem = encontrada.get();

        AtivoTravado travado = trava.travar(ordem.ticker());
        try {
            travado.ativo().confirmarCompra(ordem.quantidade());
            trava.salvarELiberar(travado);
        } catch (RuntimeException erro) {
            trava.liberar(travado);
            throw erro;
        }

        ordem.executar(Instant.now());
        ordens.salvar(ordem);
        publicador.publicar(new OrdemExecutada(ordem, Instant.now()));
    }
}
