package com.orbitapay.ativos.adapter.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.orbitapay.ativos.application.usecase.AtivoUseCase;

@Component
public class CotacaoScheduler {

    private final AtivoUseCase ativoUseCase;

    public CotacaoScheduler(AtivoUseCase ativoUseCase) {
        this.ativoUseCase = ativoUseCase;
    }

    @Scheduled(fixedDelayString = "${orbita.cotacoes.intervalo}", initialDelayString = "${orbita.cotacoes.intervalo}")
    public void atualizarCotacoesPeriodicamente() {
        ativoUseCase.simularNovasCotacoesDoMercado();
    }
}
