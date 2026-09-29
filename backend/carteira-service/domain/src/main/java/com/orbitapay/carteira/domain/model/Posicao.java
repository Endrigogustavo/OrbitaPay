package com.orbitapay.carteira.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Posicao {

    private final String ticker;
    private long quantidade;
    private BigDecimal precoMedio;
    private final Map<String, Long> reservas;

    public Posicao(String ticker, long quantidade, BigDecimal precoMedio, Map<String, Long> reservas) {
        this.ticker = Objects.requireNonNull(ticker);
        this.quantidade = quantidade;
        this.precoMedio = Objects.requireNonNull(precoMedio);
        this.reservas = new LinkedHashMap<>(reservas);
    }

    public static Posicao vazia(String ticker) {
        return new Posicao(ticker, 0, BigDecimal.ZERO, Map.of());
    }

    void adicionar(long quantidadeComprada, BigDecimal precoUnitario) {
        BigDecimal custoAtual = precoMedio.multiply(BigDecimal.valueOf(quantidade));
        BigDecimal custoNovo = precoUnitario.multiply(BigDecimal.valueOf(quantidadeComprada));
        quantidade += quantidadeComprada;
        precoMedio = custoAtual.add(custoNovo).divide(BigDecimal.valueOf(quantidade), 4, RoundingMode.HALF_EVEN);
    }

    boolean reservado(String ordemId) {
        return reservas.containsKey(ordemId);
    }

    void reservar(String ordemId, long quantidadeReservada) {
        reservas.put(ordemId, quantidadeReservada);
    }

    long liquidarReserva(String ordemId) {
        Long reservada = reservas.remove(ordemId);
        if (reservada == null) {
            return 0;
        }
        quantidade -= reservada;
        return reservada;
    }

    void cancelarReserva(String ordemId) {
        reservas.remove(ordemId);
    }

    public long quantidadeReservada() {
        return reservas.values().stream().mapToLong(Long::longValue).sum();
    }

    public long quantidadeDisponivel() {
        return quantidade - quantidadeReservada();
    }

    public boolean vazia() {
        return quantidade <= 0 && reservas.isEmpty();
    }

    public String ticker() {
        return ticker;
    }

    public long quantidade() {
        return quantidade;
    }

    public BigDecimal precoMedio() {
        return precoMedio;
    }

    public Map<String, Long> reservas() {
        return Map.copyOf(reservas);
    }
}
