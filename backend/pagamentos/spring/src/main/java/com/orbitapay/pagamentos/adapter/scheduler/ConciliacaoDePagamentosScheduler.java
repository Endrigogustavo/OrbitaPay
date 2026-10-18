package com.orbitapay.pagamentos.adapter.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.orbitapay.pagamentos.application.usecase.PagamentoUseCase;

@Component
public class ConciliacaoDePagamentosScheduler {

    private final PagamentoUseCase pagamentoUseCase;

    public ConciliacaoDePagamentosScheduler(PagamentoUseCase pagamentoUseCase) {
        this.pagamentoUseCase = pagamentoUseCase;
    }

    @Scheduled(fixedDelayString = "${orbita.conciliacao.intervalo}", initialDelayString = "${orbita.conciliacao.intervalo}")
    public void conciliarPagamentosPendentesPeriodicamente() {
        pagamentoUseCase.conciliarPagamentosPendentesComOProvedor();
    }
}
