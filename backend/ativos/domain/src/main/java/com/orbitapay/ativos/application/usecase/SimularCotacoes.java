package com.orbitapay.ativos.application.usecase;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.orbitapay.ativos.application.service.PublicadorDeEventosDeAtivo;
import com.orbitapay.ativos.application.service.SimuladorDeMercado;
import com.orbitapay.ativos.domain.event.CotacoesAtualizadas;
import com.orbitapay.ativos.domain.model.Ativo;
import com.orbitapay.ativos.domain.repository.AtivoRepository;

public class SimularCotacoes {

    private final AtivoRepository ativos;
    private final SimuladorDeMercado simulador;
    private final PublicadorDeEventosDeAtivo publicador;
    private final Clock relogio;

    public SimularCotacoes(AtivoRepository ativos, SimuladorDeMercado simulador, PublicadorDeEventosDeAtivo publicador,
            Clock relogio) {
        this.ativos = ativos;
        this.simulador = simulador;
        this.publicador = publicador;
        this.relogio = relogio;
    }

    public void executar() {
        List<Ativo> alterados = new ArrayList<>();
        for (Ativo ativo : ativos.listar()) {
            simulador.proximaCotacao(ativo.cotacao()).ifPresent(nova -> {
                ativo.registrarCotacao(nova);
                alterados.add(ativo);
            });
        }
        if (alterados.isEmpty()) {
            return;
        }
        ativos.atualizarCotacoes(alterados);
        publicador.publicar(new CotacoesAtualizadas(alterados.stream()
                .map(a -> new CotacoesAtualizadas.Cotacao(a.ticker().valor(), a.cotacao()))
                .toList(), Instant.now(relogio)));
    }
}
