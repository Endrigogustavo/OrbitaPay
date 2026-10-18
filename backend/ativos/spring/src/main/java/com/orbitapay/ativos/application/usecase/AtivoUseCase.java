package com.orbitapay.ativos.application.usecase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.orbitapay.ativos.application.exception.NaoEncontradoException;
import com.orbitapay.ativos.application.gateway.IAtivoGateway;
import com.orbitapay.ativos.application.gateway.IBolsaGateway;
import com.orbitapay.ativos.entity.Ativo;
import com.orbitapay.ativos.entity.AtivoComBolsa;
import com.orbitapay.ativos.entity.Bolsa;
import com.orbitapay.ativos.exception.RegraDeNegocioException;
import com.orbitapay.ativos.service.AtivoService;

public class AtivoUseCase {

    private static final double CHANCE_DE_A_COTACAO_MUDAR = 0.35;
    private static final double VARIACAO_MAXIMA_POR_ATUALIZACAO = 0.009;
    private static final double VARIACAO_MAXIMA_DO_HISTORICO_INICIAL = 0.018;

    private final AtivoService ativoService;
    private final IAtivoGateway ativoGateway;
    private final IBolsaGateway bolsaGateway;

    public AtivoUseCase(AtivoService ativoService, IAtivoGateway ativoGateway, IBolsaGateway bolsaGateway) {
        this.ativoService = ativoService;
        this.ativoGateway = ativoGateway;
        this.bolsaGateway = bolsaGateway;
    }

    public List<AtivoComBolsa> listarAtivosComSuasBolsas() {
        Map<String, Bolsa> bolsasPorCodigo = bolsaGateway.listarTodas().stream()
                .collect(Collectors.toMap(Bolsa::codigo, Function.identity()));
        List<AtivoComBolsa> ativosComBolsa = new ArrayList<>();
        for (Ativo ativo : ativoGateway.listarTodos()) {
            Bolsa bolsaDoAtivo = bolsasPorCodigo.get(ativo.bolsa());
            if (bolsaDoAtivo != null) {
                ativosComBolsa.add(new AtivoComBolsa(ativo, bolsaDoAtivo));
            }
        }
        return ativosComBolsa;
    }

    public AtivoComBolsa buscarAtivoComSuaBolsa(String ticker) {
        Ativo ativo = buscarAtivo(ticker);
        return new AtivoComBolsa(ativo, buscarBolsa(ativo.bolsa()));
    }

    public List<Bolsa> listarBolsas() {
        return bolsaGateway.listarTodas();
    }

    public AtivoComBolsa listarNovoAtivoNaBolsa(String ticker, String nome, String setor, String codigoDaBolsa,
            BigDecimal cotacao, Long quantidadeEmitida) {
        String tickerNormalizado = ativoService.validarENormalizarTicker(ticker);
        if (ativoGateway.buscarPorTicker(tickerNormalizado).isPresent()) {
            throw new RegraDeNegocioException(tickerNormalizado + " já está listada");
        }
        Bolsa bolsa = buscarBolsa(codigoDaBolsa);
        List<BigDecimal> historicoSimulado = cotacao == null || cotacao.signum() <= 0 ? List.of()
                : simularHistoricoAnteriorAListagem(cotacao);
        Ativo novoAtivo = ativoService.criarNovoAtivo(tickerNormalizado, nome, setor, bolsa.codigo(), cotacao,
                quantidadeEmitida, historicoSimulado, Instant.now());
        ativoGateway.salvar(novoAtivo);
        return new AtivoComBolsa(novoAtivo, bolsa);
    }

    public AtivoComBolsa atualizarCadastroDoAtivo(String ticker, String nome, String setor, String codigoDaBolsa,
            BigDecimal cotacao, Long quantidadeEmitida) {
        Bolsa bolsa = buscarBolsa(codigoDaBolsa);
        Ativo ativoAtualizado = ativoService.atualizarCadastro(buscarAtivo(ticker), nome, setor, bolsa.codigo(),
                cotacao, quantidadeEmitida);
        ativoGateway.salvar(ativoAtualizado);
        return new AtivoComBolsa(ativoAtualizado, bolsa);
    }

    public void removerAtivoDaBolsa(String ticker) {
        ativoGateway.remover(buscarAtivo(ticker).ticker());
    }

    public void retirarAcoesDoEstoque(String ticker, long quantidade) {
        ativoService.validarQuantidadeMovimentadaNoEstoque(quantidade);
        Ativo ativo = buscarAtivo(ticker);
        if (!ativoGateway.retirarDoEstoqueSeHouverQuantidade(ativo.ticker(), quantidade)) {
            long disponivelAgora = buscarAtivo(ticker).quantidadeDisponivel();
            throw new RegraDeNegocioException(ativoService.explicarFaltaDeEstoque(ativo.ticker(), disponivelAgora));
        }
    }

    public void devolverAcoesAoEstoque(String ticker, long quantidade) {
        ativoService.validarQuantidadeMovimentadaNoEstoque(quantidade);
        ativoGateway.devolverAoEstoque(buscarAtivo(ticker).ticker(), quantidade);
    }

    public void simularNovasCotacoesDoMercado() {
        ThreadLocalRandom aleatorio = ThreadLocalRandom.current();
        for (Ativo ativo : ativoGateway.listarTodos()) {
            if (aleatorio.nextDouble() >= CHANCE_DE_A_COTACAO_MUDAR) {
                continue;
            }
            double fatorDeVariacao = 1 + (aleatorio.nextDouble() - 0.485) * VARIACAO_MAXIMA_POR_ATUALIZACAO;
            BigDecimal novaCotacao = arredondarCotacao(ativo.cotacao().doubleValue() * fatorDeVariacao);
            ativoGateway.atualizarCotacao(ativo.ticker(), novaCotacao,
                    ativoService.adicionarCotacaoAoHistorico(ativo.historico(), novaCotacao));
        }
    }

    private Ativo buscarAtivo(String ticker) {
        String tickerNormalizado = ticker == null ? "" : ticker.trim().toUpperCase(Locale.ROOT);
        return ativoGateway.buscarPorTicker(tickerNormalizado)
                .orElseThrow(() -> new NaoEncontradoException("Ativo não encontrado: " + tickerNormalizado));
    }

    private Bolsa buscarBolsa(String codigoDaBolsa) {
        String codigoNormalizado = codigoDaBolsa == null ? "" : codigoDaBolsa.trim().toUpperCase(Locale.ROOT);
        return bolsaGateway.buscarPorCodigo(codigoNormalizado)
                .orElseThrow(() -> new RegraDeNegocioException("Bolsa inválida: " + codigoDaBolsa));
    }

    private static List<BigDecimal> simularHistoricoAnteriorAListagem(BigDecimal cotacaoAtual) {
        ThreadLocalRandom aleatorio = ThreadLocalRandom.current();
        List<BigDecimal> historico = new ArrayList<>();
        double cotacaoAnterior = cotacaoAtual.doubleValue();
        for (int ponto = 1; ponto < AtivoService.TAMANHO_DO_HISTORICO; ponto++) {
            cotacaoAnterior = cotacaoAnterior * (1 + (aleatorio.nextDouble() - 0.5) * VARIACAO_MAXIMA_DO_HISTORICO_INICIAL);
            historico.addFirst(arredondarCotacao(cotacaoAnterior));
        }
        return historico;
    }

    private static BigDecimal arredondarCotacao(double valor) {
        return BigDecimal.valueOf(Math.max(valor, 0.01)).setScale(2, RoundingMode.HALF_EVEN);
    }
}
