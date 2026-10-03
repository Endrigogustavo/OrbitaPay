package com.orbitapay.ativos.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.orbitapay.ativos.domain.exception.RegraDeNegocioException;

public class Ativo {

    public static final int TAMANHO_DO_HISTORICO = 40;

    private final Ticker ticker;
    private String nome;
    private String setor;
    private String bolsa;
    private BigDecimal cotacao;
    private final List<BigDecimal> historico;
    private long quantidadeEmitida;
    private final Instant listadoEm;

    public Ativo(Ticker ticker, String nome, String setor, String bolsa, BigDecimal cotacao,
            List<BigDecimal> historico, long quantidadeEmitida, Instant listadoEm) {
        this.ticker = ticker;
        this.nome = validarNome(nome);
        this.setor = normalizarSetor(setor);
        this.bolsa = bolsa;
        this.cotacao = validarCotacao(cotacao);
        this.historico = new ArrayList<>(historico);
        this.quantidadeEmitida = validarQuantidade(quantidadeEmitida);
        this.listadoEm = listadoEm;
    }

    public static Ativo listar(Ticker ticker, String nome, String setor, String bolsa, BigDecimal cotacao,
            long quantidadeEmitida, List<BigDecimal> historicoRetroativo, Instant agora) {
        List<BigDecimal> historico = new ArrayList<>(historicoRetroativo);
        historico.add(validarCotacao(cotacao));
        Ativo ativo = new Ativo(ticker, nome, setor, bolsa, cotacao, historico, quantidadeEmitida, agora);
        ativo.limitarHistorico();
        return ativo;
    }

    public void atualizarCadastro(String nome, String setor, String bolsa, BigDecimal cotacao, long quantidadeEmitida) {
        this.nome = validarNome(nome);
        this.setor = normalizarSetor(setor);
        this.bolsa = bolsa;
        this.quantidadeEmitida = validarQuantidade(quantidadeEmitida);
        if (validarCotacao(cotacao).compareTo(this.cotacao) != 0) {
            registrarCotacao(cotacao);
        }
    }

    public void registrarCotacao(BigDecimal novaCotacao) {
        this.cotacao = validarCotacao(novaCotacao);
        historico.add(this.cotacao);
        limitarHistorico();
    }

    public BigDecimal abertura() {
        return historico.isEmpty() ? cotacao : historico.getFirst();
    }

    private void limitarHistorico() {
        while (historico.size() > TAMANHO_DO_HISTORICO) {
            historico.removeFirst();
        }
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("Informe o nome da empresa");
        }
        return nome.trim();
    }

    private static String normalizarSetor(String setor) {
        return setor == null || setor.isBlank() ? "Outros" : setor.trim();
    }

    private static BigDecimal validarCotacao(BigDecimal cotacao) {
        if (cotacao == null || cotacao.signum() <= 0) {
            throw new RegraDeNegocioException("Preço precisa ser maior que zero");
        }
        return cotacao.setScale(2, RoundingMode.HALF_EVEN);
    }

    private static long validarQuantidade(long quantidade) {
        if (quantidade <= 0) {
            throw new RegraDeNegocioException("Quantidade de ações emitidas precisa ser maior que zero");
        }
        return quantidade;
    }

    public Ticker ticker() {
        return ticker;
    }

    public String nome() {
        return nome;
    }

    public String setor() {
        return setor;
    }

    public String bolsa() {
        return bolsa;
    }

    public BigDecimal cotacao() {
        return cotacao;
    }

    public List<BigDecimal> historico() {
        return List.copyOf(historico);
    }

    public long quantidadeEmitida() {
        return quantidadeEmitida;
    }

    public Instant listadoEm() {
        return listadoEm;
    }
}
