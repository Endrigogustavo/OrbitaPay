package com.orbitapay.ativos.mercado;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

import com.orbitapay.ativos.application.service.SimuladorDeMercado;

@Component
public class SimuladorDeMercadoAleatorio implements SimuladorDeMercado {

    private static final double CHANCE_DE_NEGOCIO = 0.35;
    private static final double AMPLITUDE_DO_TICK = 0.009;
    private static final double AMPLITUDE_HISTORICA = 0.018;

    @Override
    public Optional<BigDecimal> proximaCotacao(BigDecimal cotacaoAtual) {
        ThreadLocalRandom aleatorio = ThreadLocalRandom.current();
        if (aleatorio.nextDouble() >= CHANCE_DE_NEGOCIO) {
            return Optional.empty();
        }
        double fator = 1 + (aleatorio.nextDouble() - 0.485) * AMPLITUDE_DO_TICK;
        return Optional.of(arredondar(cotacaoAtual.doubleValue() * fator));
    }

    @Override
    public List<BigDecimal> historicoRetroativo(BigDecimal cotacaoAtual, int pontos) {
        ThreadLocalRandom aleatorio = ThreadLocalRandom.current();
        LinkedList<BigDecimal> historico = new LinkedList<>();
        double anterior = cotacaoAtual.doubleValue();
        for (int i = 0; i < pontos; i++) {
            anterior = anterior * (1 + (aleatorio.nextDouble() - 0.5) * AMPLITUDE_HISTORICA);
            historico.addFirst(arredondar(anterior));
        }
        return historico;
    }

    private static BigDecimal arredondar(double valor) {
        return BigDecimal.valueOf(Math.max(valor, 0.01)).setScale(2, RoundingMode.HALF_EVEN);
    }
}
