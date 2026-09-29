package com.orbitapay.ativos.application.usecase;

import java.time.Clock;
import java.time.Instant;

import com.orbitapay.ativos.application.port.in.RemoverAtivoUseCase;
import com.orbitapay.ativos.application.port.out.AtivoRepository;
import com.orbitapay.ativos.application.port.out.PublicadorDeEventosDeAtivo;
import com.orbitapay.ativos.domain.event.AtivoRemovido;
import com.orbitapay.ativos.domain.exception.AtivoNaoEncontradoException;
import com.orbitapay.ativos.domain.model.Ticker;

public class RemoverAtivo implements RemoverAtivoUseCase {

    private final AtivoRepository ativos;
    private final PublicadorDeEventosDeAtivo publicador;
    private final Clock relogio;

    public RemoverAtivo(AtivoRepository ativos, PublicadorDeEventosDeAtivo publicador, Clock relogio) {
        this.ativos = ativos;
        this.publicador = publicador;
        this.relogio = relogio;
    }

    @Override
    public void executar(String ticker) {
        Ticker codigo = new Ticker(ticker);
        if (!ativos.existe(codigo)) {
            throw new AtivoNaoEncontradoException(codigo.valor());
        }
        ativos.remover(codigo);
        publicador.publicar(new AtivoRemovido(codigo.valor(), Instant.now(relogio)));
    }
}
