package com.orbitapay.ativos.adapter.in.agendamento;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.orbitapay.ativos.application.port.in.SimularCotacoesUseCase;

@Component
public class CotacoesAgendadas {

    private final SimularCotacoesUseCase simularCotacoes;

    public CotacoesAgendadas(SimularCotacoesUseCase simularCotacoes) {
        this.simularCotacoes = simularCotacoes;
    }

    @Scheduled(fixedDelayString = "${orbita.cotacoes.intervalo-ms}", initialDelayString = "${orbita.cotacoes.intervalo-ms}")
    public void atualizar() {
        simularCotacoes.executar();
    }
}
