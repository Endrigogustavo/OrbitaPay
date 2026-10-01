package com.orbitapay.ativos.application.usecase;

import java.time.Clock;
import java.time.Instant;

import com.orbitapay.ativos.application.dto.AtivoCotado;
import com.orbitapay.ativos.application.service.PublicadorDeEventosDeAtivo;
import com.orbitapay.ativos.application.usecase.ListarAtivoNaBolsa.Comando;
import com.orbitapay.ativos.domain.event.AtivoAtualizado;
import com.orbitapay.ativos.domain.event.DadosDoAtivo;
import com.orbitapay.ativos.domain.exception.AtivoNaoEncontradoException;
import com.orbitapay.ativos.domain.exception.RegraDeNegocioException;
import com.orbitapay.ativos.domain.model.Ativo;
import com.orbitapay.ativos.domain.model.Bolsa;
import com.orbitapay.ativos.domain.model.Ticker;
import com.orbitapay.ativos.domain.repository.AtivoRepository;
import com.orbitapay.ativos.domain.repository.BolsaRepository;

public class AtualizarAtivo {

    private final AtivoRepository ativos;
    private final BolsaRepository bolsas;
    private final PublicadorDeEventosDeAtivo publicador;
    private final Clock relogio;

    public AtualizarAtivo(AtivoRepository ativos, BolsaRepository bolsas, PublicadorDeEventosDeAtivo publicador,
            Clock relogio) {
        this.ativos = ativos;
        this.bolsas = bolsas;
        this.publicador = publicador;
        this.relogio = relogio;
    }

    public AtivoCotado executar(Comando comando) {
        Ticker ticker = new Ticker(comando.ticker());
        Ativo ativo = ativos.buscar(ticker).orElseThrow(() -> new AtivoNaoEncontradoException(ticker.valor()));
        Bolsa bolsa = bolsas.buscar(comando.bolsa())
                .orElseThrow(() -> new RegraDeNegocioException("Bolsa inválida: " + comando.bolsa()));
        ativo.atualizarCadastro(comando.nome(), comando.setor(), bolsa.codigo(), comando.cotacao(),
                comando.quantidadeEmitida() == null ? ativo.quantidadeEmitida() : comando.quantidadeEmitida());
        ativos.salvar(ativo);
        publicador.publicar(new AtivoAtualizado(DadosDoAtivo.de(ativo, bolsa), Instant.now(relogio)));
        return new AtivoCotado(ativo, bolsa);
    }
}
