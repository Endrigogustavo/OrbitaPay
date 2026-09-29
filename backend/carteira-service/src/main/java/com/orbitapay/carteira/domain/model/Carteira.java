package com.orbitapay.carteira.domain.model;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.orbitapay.carteira.domain.exception.AcoesInsuficientesException;

public class Carteira {

    private final String id;
    private final String clienteId;
    private final Map<String, Posicao> posicoes;
    private final Set<String> ordensLiquidadas;

    private Carteira(String id, String clienteId, Collection<Posicao> posicoes, Collection<String> ordensLiquidadas) {
        this.id = Objects.requireNonNull(id);
        this.clienteId = Objects.requireNonNull(clienteId);
        this.posicoes = new LinkedHashMap<>();
        posicoes.forEach(p -> this.posicoes.put(p.ticker(), p));
        this.ordensLiquidadas = new LinkedHashSet<>(ordensLiquidadas);
    }

    public static Carteira abrir(String id, String clienteId) {
        return new Carteira(id, clienteId, List.of(), List.of());
    }

    public static Carteira reconstituir(String id, String clienteId, Collection<Posicao> posicoes,
            Collection<String> ordensLiquidadas) {
        return new Carteira(id, clienteId, posicoes, ordensLiquidadas);
    }

    public void registrarCompra(String ordemId, String ticker, long quantidade, BigDecimal precoUnitario) {
        if (!ordensLiquidadas.add(ordemId)) {
            return;
        }
        posicoes.computeIfAbsent(ticker, Posicao::vazia).adicionar(quantidade, precoUnitario);
    }

    public void reservarParaVenda(String ordemId, String ticker, long quantidade) {
        Posicao posicao = posicoes.get(ticker);
        if (posicao != null && posicao.reservado(ordemId)) {
            return;
        }
        if (posicao == null || posicao.quantidade() <= 0) {
            throw new AcoesInsuficientesException("Você não possui " + ticker);
        }
        if (posicao.quantidadeDisponivel() < quantidade) {
            throw new AcoesInsuficientesException("Você tem apenas " + posicao.quantidadeDisponivel() + " " + ticker
                    + " disponíveis para venda");
        }
        posicao.reservar(ordemId, quantidade);
    }

    public void liquidarVenda(String ordemId, String ticker) {
        if (!ordensLiquidadas.add(ordemId)) {
            return;
        }
        Posicao posicao = posicoes.get(ticker);
        if (posicao == null) {
            return;
        }
        posicao.liquidarReserva(ordemId);
        if (posicao.vazia()) {
            posicoes.remove(ticker);
        }
    }

    public void cancelarReserva(String ordemId, String ticker) {
        Posicao posicao = posicoes.get(ticker);
        if (posicao != null) {
            posicao.cancelarReserva(ordemId);
            if (posicao.vazia()) {
                posicoes.remove(ticker);
            }
        }
    }

    public String id() {
        return id;
    }

    public String clienteId() {
        return clienteId;
    }

    public List<Posicao> posicoes() {
        return List.copyOf(posicoes.values());
    }

    public Set<String> ordensLiquidadas() {
        return Set.copyOf(ordensLiquidadas);
    }
}
