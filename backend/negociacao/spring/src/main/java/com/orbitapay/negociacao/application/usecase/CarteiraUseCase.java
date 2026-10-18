package com.orbitapay.negociacao.application.usecase;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.orbitapay.negociacao.application.gateway.IAtivoGateway;
import com.orbitapay.negociacao.application.gateway.ICarteiraGateway;
import com.orbitapay.negociacao.application.gateway.ITransacaoGateway;
import com.orbitapay.negociacao.entity.Carteira;
import com.orbitapay.negociacao.entity.CarteiraValorizada;
import com.orbitapay.negociacao.entity.CotacaoDoAtivo;
import com.orbitapay.negociacao.service.CarteiraService;

public class CarteiraUseCase {

    private final CarteiraService carteiraService;
    private final ICarteiraGateway carteiraGateway;
    private final IAtivoGateway ativoGateway;
    private final ITransacaoGateway transacaoGateway;

    public CarteiraUseCase(CarteiraService carteiraService, ICarteiraGateway carteiraGateway, IAtivoGateway ativoGateway,
            ITransacaoGateway transacaoGateway) {
        this.carteiraService = carteiraService;
        this.carteiraGateway = carteiraGateway;
        this.ativoGateway = ativoGateway;
        this.transacaoGateway = transacaoGateway;
    }

    public CarteiraValorizada consultarCarteiraValorizada(String clienteId) {
        Carteira carteira = buscarCarteiraOuCriarVazia(clienteId);
        boolean possuiAcoes = carteira.posicoes().stream().anyMatch(posicao -> posicao.quantidade() > 0);
        Map<String, CotacaoDoAtivo> cotacoesPorTicker = possuiAcoes ? ativoGateway.buscarCotacoesDeTodosOsAtivos()
                : Map.of();
        return carteiraService.valorizarCarteira(carteira, cotacoesPorTicker);
    }

    public void adicionarAcoesCompradas(String clienteId, String ticker, long quantidade, BigDecimal precoUnitario) {
        carteiraGateway.salvar(carteiraService.adicionarAcoesCompradas(buscarCarteiraOuCriarVazia(clienteId), ticker,
                quantidade, precoUnitario));
    }

    public void retirarAcoesVendidas(String clienteId, String ticker, long quantidade) {
        transacaoGateway.executarEmTransacao(() -> carteiraGateway.salvar(
                carteiraService.retirarAcoesVendidas(buscarCarteiraOuCriarVazia(clienteId), ticker, quantidade)));
    }

    public void devolverAcoesDeVendaCancelada(String clienteId, String ticker, long quantidade) {
        transacaoGateway.executarEmTransacao(() -> carteiraGateway.salvar(
                carteiraService.devolverAcoes(buscarCarteiraOuCriarVazia(clienteId), ticker, quantidade)));
    }

    public void encerrarCarteiraDoCliente(String clienteId) {
        carteiraGateway.removerPorCliente(clienteId);
    }

    private Carteira buscarCarteiraOuCriarVazia(String clienteId) {
        return carteiraGateway.buscarPorCliente(clienteId).orElse(new Carteira(clienteId, List.of()));
    }
}
