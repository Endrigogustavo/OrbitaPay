package com.orbitapay.ativos.application.usecase;

import java.math.BigDecimal;
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

    public SimularCotacoes(AtivoRepository ativos, SimuladorDeMercado simulador, PublicadorDeEventosDeAtivo publicador) {
        this.ativos = ativos;
        this.simulador = simulador;
        this.publicador = publicador;
    }

    public void executar() {
        List<Ativo> alterados = new ArrayList<>();
        for (Ativo ativo : ativos.listar()) {
            BigDecimal novaCotacao = simulador.proximaCotacao(ativo.cotacao());
            if (novaCotacao != null) {
                ativo.registrarCotacao(novaCotacao);
                alterados.add(ativo);
            }
        }
        if (alterados.isEmpty()) {
            return;
        }
        ativos.atualizarCotacoes(alterados);
        List<CotacoesAtualizadas.Cotacao> cotacoes = new ArrayList<>();
        for (Ativo ativo : alterados) {
            cotacoes.add(new CotacoesAtualizadas.Cotacao(ativo.ticker().valor(), ativo.cotacao()));
        }
        publicador.publicar(new CotacoesAtualizadas(cotacoes, Instant.now()));
    }
}
