package com.orbitapay.negociacao.application.usecase;

import java.time.Instant;
import java.util.Optional;

import com.orbitapay.negociacao.application.dto.AtivoDoCatalogo;
import com.orbitapay.negociacao.application.service.CatalogoDeAtivos;
import com.orbitapay.negociacao.application.service.PublicadorDeEventosDeOrdem;
import com.orbitapay.negociacao.domain.event.OrdemDeCompraSolicitada;
import com.orbitapay.negociacao.domain.event.OrdemDeVendaSolicitada;
import com.orbitapay.negociacao.domain.exception.RecursoNaoEncontradoException;
import com.orbitapay.negociacao.domain.model.AtivoNegociavel;
import com.orbitapay.negociacao.domain.model.Investidor;
import com.orbitapay.negociacao.domain.model.Ordem;
import com.orbitapay.negociacao.domain.model.TipoOrdem;
import com.orbitapay.negociacao.domain.repository.AtivoNegociavelRepository;
import com.orbitapay.negociacao.domain.repository.AtivoTravado;
import com.orbitapay.negociacao.domain.repository.InvestidorRepository;
import com.orbitapay.negociacao.domain.repository.OrdemRepository;
import com.orbitapay.negociacao.domain.repository.TravaDeAtivo;

public class EnviarOrdem {

    public record Comando(String clienteId, String ticker, String tipo, int quantidade) {
    }

    private final OrdemRepository ordens;
    private final AtivoNegociavelRepository ativos;
    private final InvestidorRepository investidores;
    private final CatalogoDeAtivos catalogo;
    private final TravaDeAtivo trava;
    private final PublicadorDeEventosDeOrdem publicador;

    public EnviarOrdem(OrdemRepository ordens, AtivoNegociavelRepository ativos, InvestidorRepository investidores,
            CatalogoDeAtivos catalogo, TravaDeAtivo trava, PublicadorDeEventosDeOrdem publicador) {
        this.ordens = ordens;
        this.ativos = ativos;
        this.investidores = investidores;
        this.catalogo = catalogo;
        this.trava = trava;
        this.publicador = publicador;
    }

    public Ordem executar(Comando comando) {
        TipoOrdem tipo = TipoOrdem.de(comando.tipo());
        Ordem.validarQuantidade(comando.quantidade());
        exigirInvestidorLiberado(comando.clienteId());
        String ticker = comando.ticker() == null ? "" : comando.ticker().trim().toUpperCase();
        garantirAtivoConhecido(ticker);
        if (tipo == TipoOrdem.COMPRA) {
            return comprar(comando, ticker);
        }
        return vender(comando, ticker);
    }

    private Ordem comprar(Comando comando, String ticker) {
        AtivoTravado travado = trava.travar(ticker);
        Ordem ordem;
        try {
            AtivoNegociavel ativo = travado.ativo();
            ativo.reservarParaCompra(comando.quantidade());
            ordem = Ordem.abrir(ordens.proximoId(), comando.clienteId(), ativo, TipoOrdem.COMPRA,
                    comando.quantidade(), Instant.now());
            trava.salvarELiberar(travado);
        } catch (RuntimeException erro) {
            trava.liberar(travado);
            throw erro;
        }
        ordens.salvar(ordem);
        publicador.publicar(new OrdemDeCompraSolicitada(ordem, Instant.now()));
        return ordem;
    }

    private Ordem vender(Comando comando, String ticker) {
        Optional<AtivoNegociavel> encontrado = ativos.buscar(ticker);
        if (encontrado.isEmpty()) {
            throw new RecursoNaoEncontradoException("Ativo não negociado: " + ticker);
        }
        AtivoNegociavel ativo = encontrado.get();
        ativo.exigirNegociavel();
        Ordem ordem = Ordem.abrir(ordens.proximoId(), comando.clienteId(), ativo, TipoOrdem.VENDA,
                comando.quantidade(), Instant.now());
        ordens.salvar(ordem);
        publicador.publicar(new OrdemDeVendaSolicitada(ordem, Instant.now()));
        return ordem;
    }

    private void exigirInvestidorLiberado(String clienteId) {
        Optional<Investidor> investidor = investidores.buscar(clienteId);
        if (investidor.isPresent()) {
            investidor.get().exigirLiberado();
        }
    }

    private void garantirAtivoConhecido(String ticker) {
        if (ativos.existe(ticker)) {
            return;
        }
        Optional<AtivoDoCatalogo> dados = catalogo.consultar(ticker);
        if (dados.isEmpty()) {
            throw new RecursoNaoEncontradoException("Ativo não negociado: " + ticker);
        }
        AtivoDoCatalogo ativo = dados.get();
        ativos.inserir(AtivoNegociavel.listar(ativo.ticker(), ativo.nome(), ativo.bolsa(), ativo.moeda(),
                ativo.cambio(), ativo.cotacao(), ativo.quantidadeEmitida()));
    }
}
