package com.orbitapay.ativos.agendamento;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.orbitapay.ativos.application.usecase.SimularCotacoes;

@Component
public class CotacoesAgendadas {

    private final SimularCotacoes simularCotacoes;

    public CotacoesAgendadas(SimularCotacoes simularCotacoes) {
        this.simularCotacoes = simularCotacoes;
    }

    @Scheduled(fixedDelayString = "${orbita.cotacoes.intervalo-ms}", initialDelayString = "${orbita.cotacoes.intervalo-ms}")
    public void atualizar() {
        simularCotacoes.executar();
    }
}
