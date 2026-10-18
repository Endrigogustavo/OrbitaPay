package com.orbitapay.negociacao.application.usecase;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.orbitapay.negociacao.application.exception.NaoEncontradoException;
import com.orbitapay.negociacao.application.gateway.IAtivoGateway;
import com.orbitapay.negociacao.application.gateway.IClienteGateway;
import com.orbitapay.negociacao.application.gateway.IEventoGateway;
import com.orbitapay.negociacao.application.gateway.IOrdemGateway;
import com.orbitapay.negociacao.application.usecase.execucao.ExecucaoDeCompra;
import com.orbitapay.negociacao.application.usecase.execucao.ExecucaoDeOrdem;
import com.orbitapay.negociacao.application.usecase.execucao.ExecucaoDeVenda;
import com.orbitapay.negociacao.entity.CotacaoDoAtivo;
import com.orbitapay.negociacao.entity.Ordem;
import com.orbitapay.negociacao.entity.StatusOrdem;
import com.orbitapay.negociacao.entity.TipoOrdem;
import com.orbitapay.negociacao.service.OrdemService;

public class OrdemUseCase {

    private final OrdemService ordemService;
    private final IOrdemGateway ordemGateway;
    private final IAtivoGateway ativoGateway;
    private final IClienteGateway clienteGateway;
    private final IEventoGateway eventoGateway;
    private final ExecucaoDeCompra execucaoDeCompra;
    private final ExecucaoDeVenda execucaoDeVenda;

    public OrdemUseCase(OrdemService ordemService, IOrdemGateway ordemGateway, IAtivoGateway ativoGateway,
            IClienteGateway clienteGateway, IEventoGateway eventoGateway, ExecucaoDeCompra execucaoDeCompra,
            ExecucaoDeVenda execucaoDeVenda) {
        this.ordemService = ordemService;
        this.ordemGateway = ordemGateway;
        this.ativoGateway = ativoGateway;
        this.clienteGateway = clienteGateway;
        this.eventoGateway = eventoGateway;
        this.execucaoDeCompra = execucaoDeCompra;
        this.execucaoDeVenda = execucaoDeVenda;
    }

    public Ordem solicitarOrdem(String clienteId, String ticker, String tipoInformado, Integer quantidadeInformada) {
        TipoOrdem tipo = ordemService.converterTipoDaOrdem(tipoInformado);
        int quantidade = ordemService.validarQuantidade(quantidadeInformada);
        ordemService.garantirQueClientePodeNegociar(clienteGateway.clienteEstaBloqueado(clienteId));

        String tickerNormalizado = ticker == null ? "" : ticker.trim().toUpperCase(Locale.ROOT);
        CotacaoDoAtivo ativo = ativoGateway.buscarCotacaoDoAtivo(tickerNormalizado)
                .orElseThrow(() -> new NaoEncontradoException("Ativo não negociado: " + tickerNormalizado));

        Ordem ordemPendente = ordemService.criarOrdemPendente(UUID.randomUUID().toString(), clienteId, ativo, tipo,
                quantidade, Instant.now());
        ordemGateway.salvar(ordemPendente);
        eventoGateway.publicarOrdemSolicitada(ordemPendente);
        return ordemPendente;
    }

    public void executarOrdemSolicitada(String ordemId) {
        ordemGateway.buscarPorId(ordemId)
                .filter(ordem -> ordem.status() == StatusOrdem.PENDENTE)
                .ifPresent(ordemPendente -> escolherExecucaoPeloTipo(ordemPendente.tipo()).executar(ordemPendente));
    }

    public Ordem buscarOrdemDoCliente(String ordemId, String clienteId) {
        return ordemGateway.buscarPorId(ordemId)
                .filter(ordem -> ordem.clienteId().equals(clienteId))
                .orElseThrow(() -> new NaoEncontradoException("Ordem não encontrada: " + ordemId));
    }

    public List<Ordem> listarOrdensDoCliente(String clienteId) {
        return ordemGateway.listarDoCliente(clienteId);
    }

    private ExecucaoDeOrdem escolherExecucaoPeloTipo(TipoOrdem tipo) {
        return tipo == TipoOrdem.COMPRA ? execucaoDeCompra : execucaoDeVenda;
    }
}
