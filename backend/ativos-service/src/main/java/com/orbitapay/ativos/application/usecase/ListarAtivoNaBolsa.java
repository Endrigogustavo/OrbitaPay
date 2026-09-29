package com.orbitapay.ativos.application.usecase;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import com.orbitapay.ativos.application.dto.AtivoCotado;
import com.orbitapay.ativos.application.port.in.ListarAtivoNaBolsaUseCase;
import com.orbitapay.ativos.application.port.out.AtivoRepository;
import com.orbitapay.ativos.application.port.out.BolsaRepository;
import com.orbitapay.ativos.application.port.out.PublicadorDeEventosDeAtivo;
import com.orbitapay.ativos.application.port.out.SimuladorDeMercado;
import com.orbitapay.ativos.domain.event.AtivoListado;
import com.orbitapay.ativos.domain.event.DadosDoAtivo;
import com.orbitapay.ativos.domain.exception.RegraDeNegocioException;
import com.orbitapay.ativos.domain.model.Ativo;
import com.orbitapay.ativos.domain.model.Bolsa;
import com.orbitapay.ativos.domain.model.Ticker;

public class ListarAtivoNaBolsa implements ListarAtivoNaBolsaUseCase {

    public static final long EMISSAO_PADRAO = 1_000_000L;

    private final AtivoRepository ativos;
    private final BolsaRepository bolsas;
    private final SimuladorDeMercado simulador;
    private final PublicadorDeEventosDeAtivo publicador;
    private final Clock relogio;

    public ListarAtivoNaBolsa(AtivoRepository ativos, BolsaRepository bolsas, SimuladorDeMercado simulador,
            PublicadorDeEventosDeAtivo publicador, Clock relogio) {
        this.ativos = ativos;
        this.bolsas = bolsas;
        this.simulador = simulador;
        this.publicador = publicador;
        this.relogio = relogio;
    }

    @Override
    public AtivoCotado executar(Comando comando) {
        Ticker ticker = new Ticker(comando.ticker());
        if (ativos.existe(ticker)) {
            throw new RegraDeNegocioException(ticker.valor() + " já está listada");
        }
        Bolsa bolsa = bolsas.buscar(comando.bolsa())
                .orElseThrow(() -> new RegraDeNegocioException("Bolsa inválida: " + comando.bolsa()));
        Instant agora = Instant.now(relogio);
        Ativo ativo = Ativo.listar(ticker, comando.nome(), comando.setor(), bolsa.codigo(), comando.cotacao(),
                comando.quantidadeEmitida() == null ? EMISSAO_PADRAO : comando.quantidadeEmitida(),
                comando.cotacao() == null || comando.cotacao().signum() <= 0 ? List.of()
                        : simulador.historicoRetroativo(comando.cotacao(), Ativo.TAMANHO_DO_HISTORICO - 1),
                agora);
        ativos.salvar(ativo);
        publicador.publicar(new AtivoListado(DadosDoAtivo.de(ativo, bolsa), agora));
        return new AtivoCotado(ativo, bolsa);
    }
}
