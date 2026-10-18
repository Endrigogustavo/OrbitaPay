package com.orbitapay.negociacao.adapter.gateway.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.orbitapay.negociacao.adapter.gateway.http.dto.response.AtivoResponse;
import com.orbitapay.negociacao.entity.CotacaoDoAtivo;
import com.orbitapay.negociacao.application.gateway.IAtivoGateway;

@Component
public class AtivoHttpGateway extends GatewayHttpBase implements IAtivoGateway {

    public AtivoHttpGateway(RestClient ativosRestClient) {
        super(ativosRestClient, "de ativos");
    }

    @Override
    public Optional<CotacaoDoAtivo> buscarCotacaoDoAtivo(String ticker) {
        return buscarNoServico(AtivoResponse.class, "/ativos/{ticker}", ticker)
                .map(AtivoHttpGateway::converterParaCotacao);
    }

    @Override
    public Map<String, CotacaoDoAtivo> buscarCotacoesDeTodosOsAtivos() {
        List<AtivoResponse> ativos = buscarListaNoServico(new ParameterizedTypeReference<List<AtivoResponse>>() {
        }, "/ativos");
        Map<String, CotacaoDoAtivo> cotacoesPorTicker = new HashMap<>();
        for (AtivoResponse ativo : ativos == null ? List.<AtivoResponse>of() : ativos) {
            cotacoesPorTicker.put(ativo.ticker(), converterParaCotacao(ativo));
        }
        return cotacoesPorTicker;
    }

    @Override
    public void retirarAcoesDoEstoque(String ticker, int quantidade) {
        enviarPostParaOServico("/interno/ativos/{ticker}/estoque/retiradas", Map.of("quantidade", quantidade), ticker);
    }

    @Override
    public void devolverAcoesAoEstoque(String ticker, int quantidade) {
        enviarPostParaOServico("/interno/ativos/{ticker}/estoque/devolucoes", Map.of("quantidade", quantidade), ticker);
    }

    private static CotacaoDoAtivo converterParaCotacao(AtivoResponse ativo) {
        return new CotacaoDoAtivo(ativo.ticker(), ativo.nome(), ativo.moeda(), ativo.cambio(), ativo.cotacao());
    }
}
