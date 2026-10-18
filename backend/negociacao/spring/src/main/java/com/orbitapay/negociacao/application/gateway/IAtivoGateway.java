package com.orbitapay.negociacao.application.gateway;

import java.util.Map;
import java.util.Optional;

import com.orbitapay.negociacao.entity.CotacaoDoAtivo;

public interface IAtivoGateway {

    Optional<CotacaoDoAtivo> buscarCotacaoDoAtivo(String ticker);

    Map<String, CotacaoDoAtivo> buscarCotacoesDeTodosOsAtivos();

    void retirarAcoesDoEstoque(String ticker, int quantidade);

    void devolverAcoesAoEstoque(String ticker, int quantidade);
}
