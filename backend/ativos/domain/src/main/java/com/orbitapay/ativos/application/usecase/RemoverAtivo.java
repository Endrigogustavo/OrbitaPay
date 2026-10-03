package com.orbitapay.ativos.application.usecase;

import java.time.Instant;

import com.orbitapay.ativos.application.service.PublicadorDeEventosDeAtivo;
import com.orbitapay.ativos.domain.event.AtivoRemovido;
import com.orbitapay.ativos.domain.exception.AtivoNaoEncontradoException;
import com.orbitapay.ativos.domain.model.Ticker;
import com.orbitapay.ativos.domain.repository.AtivoRepository;

public class RemoverAtivo {

    private final AtivoRepository ativos;
    private final PublicadorDeEventosDeAtivo publicador;

    public RemoverAtivo(AtivoRepository ativos, PublicadorDeEventosDeAtivo publicador) {
        this.ativos = ativos;
        this.publicador = publicador;
    }

    public void executar(String ticker) {
        Ticker codigo = new Ticker(ticker);
        if (!ativos.existe(codigo)) {
            throw new AtivoNaoEncontradoException(codigo.valor());
        }
        ativos.remover(codigo);
        publicador.publicar(new AtivoRemovido(codigo.valor(), Instant.now()));
    }
}
