package com.orbitapay.negociacao.application.usecase;

import java.time.Clock;
import java.time.Instant;

import com.orbitapay.negociacao.application.port.in.EnviarOrdemUseCase;
import com.orbitapay.negociacao.application.port.out.AtivoNegociavelRepository;
import com.orbitapay.negociacao.application.port.out.CatalogoDeAtivos;
import com.orbitapay.negociacao.application.port.out.InvestidorRepository;
import com.orbitapay.negociacao.application.port.out.OrdemRepository;
import com.orbitapay.negociacao.application.port.out.PublicadorDeEventosDeOrdem;
import com.orbitapay.negociacao.domain.event.OrdemDeCompraSolicitada;
import com.orbitapay.negociacao.domain.event.OrdemDeVendaSolicitada;
import com.orbitapay.negociacao.domain.exception.RecursoNaoEncontradoException;
import com.orbitapay.negociacao.domain.model.AtivoNegociavel;
import com.orbitapay.negociacao.domain.model.Investidor;
import com.orbitapay.negociacao.domain.model.Ordem;
import com.orbitapay.negociacao.domain.model.TipoOrdem;

public class EnviarOrdem implements EnviarOrdemUseCase {

    private final OrdemRepository ordens;
    private final AtivoNegociavelRepository ativos;
    private final InvestidorRepository investidores;
    private final CatalogoDeAtivos catalogo;
    private final OperacaoComTrava operacao;
    private final PublicadorDeEventosDeOrdem publicador;
    private final Clock relogio;

    public EnviarOrdem(OrdemRepository ordens, AtivoNegociavelRepository ativos, InvestidorRepository investidores,
            CatalogoDeAtivos catalogo, OperacaoComTrava operacao, PublicadorDeEventosDeOrdem publicador,
            Clock relogio) {
        this.ordens = ordens;
        this.ativos = ativos;
        this.investidores = investidores;
        this.catalogo = catalogo;
        this.operacao = operacao;
        this.publicador = publicador;
        this.relogio = relogio;
    }

    @Override
    public Ordem executar(Comando comando) {
        TipoOrdem tipo = TipoOrdem.de(comando.tipo());
        Ordem.validarQuantidade(comando.quantidade());
        investidores.buscar(comando.clienteId()).ifPresent(Investidor::exigirLiberado);
        String ticker = comando.ticker() == null ? "" : comando.ticker().trim().toUpperCase();
        garantirAtivoConhecido(ticker);
        return tipo == TipoOrdem.COMPRA ? comprar(comando, ticker) : vender(comando, ticker);
    }

    private Ordem comprar(Comando comando, String ticker) {
        Ordem ordem = operacao.executar(ticker, ativo -> {
            ativo.reservarParaCompra(comando.quantidade());
            return Ordem.abrir(ordens.proximoId(), comando.clienteId(), ativo, TipoOrdem.COMPRA,
                    comando.quantidade(), Instant.now(relogio));
        });
        ordens.salvar(ordem);
        publicador.publicar(new OrdemDeCompraSolicitada(ordem, Instant.now(relogio)));
        return ordem;
    }

    private Ordem vender(Comando comando, String ticker) {
        AtivoNegociavel ativo = ativos.buscar(ticker).orElseThrow(() -> naoEncontrado(ticker));
        ativo.exigirNegociavel();
        Ordem ordem = Ordem.abrir(ordens.proximoId(), comando.clienteId(), ativo, TipoOrdem.VENDA,
                comando.quantidade(), Instant.now(relogio));
        ordens.salvar(ordem);
        publicador.publicar(new OrdemDeVendaSolicitada(ordem, Instant.now(relogio)));
        return ordem;
    }

    private void garantirAtivoConhecido(String ticker) {
        if (ativos.existe(ticker)) {
            return;
        }
        AtivoNegociavel ativo = catalogo.consultar(ticker)
                .map(dados -> AtivoNegociavel.listar(dados.ticker(), dados.nome(), dados.bolsa(), dados.moeda(),
                        dados.cambio(), dados.cotacao(), dados.quantidadeEmitida()))
                .orElseThrow(() -> naoEncontrado(ticker));
        ativos.inserir(ativo);
    }

    private static RecursoNaoEncontradoException naoEncontrado(String ticker) {
        return new RecursoNaoEncontradoException("Ativo não negociado: " + ticker);
    }
}
