package com.orbitapay.pagamentos.agendamento;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.orbitapay.pagamentos.application.usecase.ConciliarPagamentos;

@Component
public class ConciliacaoAgendada {

    private static final Logger LOG = LoggerFactory.getLogger(ConciliacaoAgendada.class);

    private final ConciliarPagamentos conciliarPagamentos;

    public ConciliacaoAgendada(ConciliarPagamentos conciliarPagamentos) {
        this.conciliarPagamentos = conciliarPagamentos;
    }

    @Scheduled(fixedDelayString = "${orbita.conciliacao.intervalo-ms}", initialDelayString = "${orbita.conciliacao.intervalo-ms}")
    public void conciliar() {
        ConciliarPagamentos.Resultado resultado = conciliarPagamentos.executar();
        if (resultado.confirmados() + resultado.expirados() + resultado.falhas() > 0) {
            LOG.info("CONCILIAÇÃO | verificados={} confirmados={} expirados={} falhas={}", resultado.verificados(),
                    resultado.confirmados(), resultado.expirados(), resultado.falhas());
        }
    }
}
