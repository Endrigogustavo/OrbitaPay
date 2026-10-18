package com.orbitapay.ativos.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.orbitapay.ativos.entity.Ativo;
import com.orbitapay.ativos.exception.RegraDeNegocioException;

public class AtivoService {

    public static final int TAMANHO_DO_HISTORICO = 40;
    private static final long QUANTIDADE_EMITIDA_PADRAO = 1_000_000L;

    public String validarENormalizarTicker(String ticker) {
        String tickerNormalizado = ticker == null ? "" : ticker.trim().toUpperCase(Locale.ROOT);
        if (!tickerNormalizado.matches("[A-Z0-9]{2,8}")) {
            throw new RegraDeNegocioException("Ticker com 2 a 8 letras ou números");
        }
        return tickerNormalizado;
    }

    public Ativo criarNovoAtivo(String ticker, String nome, String setor, String codigoDaBolsa, BigDecimal cotacao,
            Long quantidadeEmitida, List<BigDecimal> historicoAnteriorAListagem, Instant agora) {
        BigDecimal cotacaoInicial = validarCotacao(cotacao);
        long acoesEmitidas = validarQuantidadeEmitida(quantidadeEmitida, QUANTIDADE_EMITIDA_PADRAO);
        List<BigDecimal> historico = adicionarCotacaoAoHistorico(historicoAnteriorAListagem, cotacaoInicial);
        return new Ativo(validarENormalizarTicker(ticker), validarNome(nome), normalizarSetor(setor), codigoDaBolsa,
                cotacaoInicial, historico, acoesEmitidas, acoesEmitidas, agora);
    }

    public Ativo atualizarCadastro(Ativo ativoAtual, String nome, String setor, String codigoDaBolsa,
            BigDecimal cotacao, Long quantidadeEmitida) {
        BigDecimal novaCotacao = validarCotacao(cotacao);
        long novaQuantidadeEmitida = validarQuantidadeEmitida(quantidadeEmitida, ativoAtual.quantidadeEmitida());
        long acoesAcrescentadasAoMercado = novaQuantidadeEmitida - ativoAtual.quantidadeEmitida();
        long novaQuantidadeDisponivel = Math.max(0, ativoAtual.quantidadeDisponivel() + acoesAcrescentadasAoMercado);
        List<BigDecimal> historico = novaCotacao.compareTo(ativoAtual.cotacao()) == 0
                ? ativoAtual.historico()
                : adicionarCotacaoAoHistorico(ativoAtual.historico(), novaCotacao);
        return new Ativo(ativoAtual.ticker(), validarNome(nome), normalizarSetor(setor), codigoDaBolsa, novaCotacao,
                historico, novaQuantidadeEmitida, novaQuantidadeDisponivel, ativoAtual.listadoEm());
    }

    public List<BigDecimal> adicionarCotacaoAoHistorico(List<BigDecimal> historico, BigDecimal novaCotacao) {
        List<BigDecimal> historicoAtualizado = new ArrayList<>(historico);
        historicoAtualizado.add(novaCotacao);
        while (historicoAtualizado.size() > TAMANHO_DO_HISTORICO) {
            historicoAtualizado.removeFirst();
        }
        return historicoAtualizado;
    }

    public void validarQuantidadeMovimentadaNoEstoque(long quantidade) {
        if (quantidade <= 0) {
            throw new RegraDeNegocioException("Informe uma quantidade maior que zero");
        }
    }

    public String explicarFaltaDeEstoque(String ticker, long quantidadeDisponivel) {
        return quantidadeDisponivel == 0
                ? "Não há ações de " + ticker + " disponíveis no mercado"
                : "Apenas " + quantidadeDisponivel + " ações de " + ticker + " disponíveis no mercado";
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

    private static long validarQuantidadeEmitida(Long quantidadeEmitida, long valorQuandoNaoInformada) {
        long quantidade = quantidadeEmitida == null ? valorQuandoNaoInformada : quantidadeEmitida;
        if (quantidade <= 0) {
            throw new RegraDeNegocioException("Quantidade de ações emitidas precisa ser maior que zero");
        }
        return quantidade;
    }
}
