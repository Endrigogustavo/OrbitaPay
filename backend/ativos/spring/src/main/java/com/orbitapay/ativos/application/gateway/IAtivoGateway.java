package com.orbitapay.ativos.application.gateway;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.orbitapay.ativos.entity.Ativo;

public interface IAtivoGateway {

    void salvar(Ativo ativo);

    Optional<Ativo> buscarPorTicker(String ticker);

    List<Ativo> listarTodos();

    void remover(String ticker);

    boolean retirarDoEstoqueSeHouverQuantidade(String ticker, long quantidade);

    void devolverAoEstoque(String ticker, long quantidade);

    void atualizarCotacao(String ticker, BigDecimal novaCotacao, List<BigDecimal> historicoAtualizado);
}
