package com.orbitapay.negociacao.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import com.orbitapay.negociacao.domain.exception.OfertaInsuficienteException;
import com.orbitapay.negociacao.domain.exception.RegraDeNegocioException;

public class AtivoNegociavel {

    private final String ticker;
    private String nome;
    private String bolsa;
    private String moeda;
    private BigDecimal cambio;
    private BigDecimal cotacao;
    private long quantidadeEmitida;
    private long quantidadeDisponivel;
    private long quantidadeReservada;
    private boolean negociavel;

    private AtivoNegociavel(String ticker, String nome, String bolsa, String moeda, BigDecimal cambio,
            BigDecimal cotacao, long quantidadeEmitida, long quantidadeDisponivel, long quantidadeReservada,
            boolean negociavel) {
        this.ticker = Objects.requireNonNull(ticker);
        this.nome = nome;
        this.bolsa = bolsa;
        this.moeda = moeda;
        this.cambio = Objects.requireNonNull(cambio);
        this.cotacao = Objects.requireNonNull(cotacao);
        this.quantidadeEmitida = quantidadeEmitida;
        this.quantidadeDisponivel = quantidadeDisponivel;
        this.quantidadeReservada = quantidadeReservada;
        this.negociavel = negociavel;
    }

    public static AtivoNegociavel listar(String ticker, String nome, String bolsa, String moeda, BigDecimal cambio,
            BigDecimal cotacao, long quantidadeEmitida) {
        return new AtivoNegociavel(ticker, nome, bolsa, moeda, cambio, cotacao, quantidadeEmitida, quantidadeEmitida,
                0, true);
    }

    public static AtivoNegociavel reconstituir(String ticker, String nome, String bolsa, String moeda,
            BigDecimal cambio, BigDecimal cotacao, long quantidadeEmitida, long quantidadeDisponivel,
            long quantidadeReservada, boolean negociavel) {
        return new AtivoNegociavel(ticker, nome, bolsa, moeda, cambio, cotacao, quantidadeEmitida,
                quantidadeDisponivel, quantidadeReservada, negociavel);
    }

    public void reservarParaCompra(int quantidade) {
        exigirNegociavel();
        if (quantidadeDisponivel < quantidade) {
            throw new OfertaInsuficienteException(ticker, quantidadeDisponivel);
        }
        quantidadeDisponivel -= quantidade;
        quantidadeReservada += quantidade;
    }

    public void confirmarCompra(int quantidade) {
        quantidadeReservada = Math.max(0, quantidadeReservada - quantidade);
    }

    public void cancelarReserva(int quantidade) {
        quantidadeReservada = Math.max(0, quantidadeReservada - quantidade);
        quantidadeDisponivel += quantidade;
    }

    public void receberVenda(int quantidade) {
        quantidadeDisponivel += quantidade;
    }

    public void atualizarCadastro(String nome, String bolsa, String moeda, BigDecimal cambio, BigDecimal cotacao,
            long novaQuantidadeEmitida) {
        this.nome = nome;
        this.bolsa = bolsa;
        this.moeda = moeda;
        this.cambio = Objects.requireNonNull(cambio);
        this.cotacao = Objects.requireNonNull(cotacao);
        this.quantidadeDisponivel = Math.max(0, quantidadeDisponivel + (novaQuantidadeEmitida - quantidadeEmitida));
        this.quantidadeEmitida = novaQuantidadeEmitida;
        this.negociavel = true;
    }

    public void atualizarCotacao(BigDecimal cotacao) {
        this.cotacao = Objects.requireNonNull(cotacao);
    }

    public void retirarDeNegociacao() {
        this.negociavel = false;
    }

    public BigDecimal valorEmReais(int quantidade) {
        return cotacao.multiply(BigDecimal.valueOf(quantidade)).multiply(cambio).setScale(2, RoundingMode.HALF_EVEN);
    }

    public void exigirNegociavel() {
        if (!negociavel) {
            throw new RegraDeNegocioException(ticker + " não está mais negociável");
        }
    }

    public String ticker() {
        return ticker;
    }

    public String nome() {
        return nome;
    }

    public String bolsa() {
        return bolsa;
    }

    public String moeda() {
        return moeda;
    }

    public BigDecimal cambio() {
        return cambio;
    }

    public BigDecimal cotacao() {
        return cotacao;
    }

    public long quantidadeEmitida() {
        return quantidadeEmitida;
    }

    public long quantidadeDisponivel() {
        return quantidadeDisponivel;
    }

    public long quantidadeReservada() {
        return quantidadeReservada;
    }

    public boolean negociavel() {
        return negociavel;
    }
}
