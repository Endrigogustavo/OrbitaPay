package com.orbitapay.negociacao.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

import com.orbitapay.negociacao.domain.exception.RegraDeNegocioException;

public class Ordem {

    public static final int QUANTIDADE_MAXIMA = 9999;

    private final String id;
    private final String clienteId;
    private final String ticker;
    private final TipoOrdem tipo;
    private final int quantidade;
    private final BigDecimal precoUnitario;
    private final String moeda;
    private final BigDecimal cambio;
    private final BigDecimal valorTotal;
    private StatusOrdem status;
    private String motivoRejeicao;
    private final Instant criadaEm;
    private Instant atualizadaEm;

    public Ordem(String id, String clienteId, String ticker, TipoOrdem tipo, int quantidade, BigDecimal precoUnitario,
            String moeda, BigDecimal cambio, BigDecimal valorTotal, StatusOrdem status, String motivoRejeicao,
            Instant criadaEm, Instant atualizadaEm) {
        this.id = id;
        this.clienteId = clienteId;
        this.ticker = ticker;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.moeda = moeda;
        this.cambio = cambio;
        this.valorTotal = valorTotal;
        this.status = status;
        this.motivoRejeicao = motivoRejeicao;
        this.criadaEm = criadaEm;
        this.atualizadaEm = atualizadaEm;
    }

    public static Ordem abrir(String id, String clienteId, AtivoNegociavel ativo, TipoOrdem tipo, int quantidade,
            Instant agora) {
        validarQuantidade(quantidade);
        return new Ordem(id, clienteId, ativo.ticker(), tipo, quantidade, ativo.cotacao(), ativo.moeda(),
                ativo.cambio(), ativo.valorEmReais(quantidade), StatusOrdem.PENDENTE, null, agora, agora);
    }

    public static void validarQuantidade(int quantidade) {
        if (quantidade < 1 || quantidade > QUANTIDADE_MAXIMA) {
            throw new RegraDeNegocioException("Quantidade deve ficar entre 1 e " + QUANTIDADE_MAXIMA + " ações");
        }
    }

    public void executar(Instant agora) {
        exigirPendente();
        this.status = StatusOrdem.EXECUTADA;
        this.atualizadaEm = agora;
    }

    public void rejeitar(String motivo, Instant agora) {
        exigirPendente();
        this.status = StatusOrdem.REJEITADA;
        this.motivoRejeicao = motivo;
        this.atualizadaEm = agora;
    }

    public boolean pendente() {
        return status == StatusOrdem.PENDENTE;
    }

    public boolean pertenceA(String clienteId) {
        return this.clienteId.equals(clienteId);
    }

    private void exigirPendente() {
        if (!pendente()) {
            throw new RegraDeNegocioException("A ordem " + id + " já foi finalizada como " + status);
        }
    }

    public String id() {
        return id;
    }

    public String clienteId() {
        return clienteId;
    }

    public String ticker() {
        return ticker;
    }

    public TipoOrdem tipo() {
        return tipo;
    }

    public int quantidade() {
        return quantidade;
    }

    public BigDecimal precoUnitario() {
        return precoUnitario;
    }

    public String moeda() {
        return moeda;
    }

    public BigDecimal cambio() {
        return cambio;
    }

    public BigDecimal valorTotal() {
        return valorTotal;
    }

    public StatusOrdem status() {
        return status;
    }

    public String motivoRejeicao() {
        return motivoRejeicao;
    }

    public Instant criadaEm() {
        return criadaEm;
    }

    public Instant atualizadaEm() {
        return atualizadaEm;
    }
}
