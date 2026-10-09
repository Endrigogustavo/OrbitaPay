package com.orbitapay.carteira.application.usecase;

import java.time.Instant;

import com.orbitapay.carteira.application.dto.OrdemRecebida;
import com.orbitapay.carteira.application.service.PublicadorDeEventosDeCarteira;
import com.orbitapay.carteira.domain.event.AcoesInsuficientes;
import com.orbitapay.carteira.domain.event.AcoesReservadas;
import com.orbitapay.carteira.domain.exception.AcoesInsuficientesException;
import com.orbitapay.carteira.domain.repository.CarteiraTravada;
import com.orbitapay.carteira.domain.repository.TravaDeCarteira;

public class ReservarAcoesParaVenda {

    private final TravaDeCarteira trava;
    private final PublicadorDeEventosDeCarteira publicador;

    public ReservarAcoesParaVenda(TravaDeCarteira trava, PublicadorDeEventosDeCarteira publicador) {
        this.trava = trava;
        this.publicador = publicador;
    }

    public void executar(OrdemRecebida ordem) {
        CarteiraTravada travada = trava.travar(ordem.clienteId());
        try {
            travada.carteira().reservarParaVenda(ordem.ordemId(), ordem.ticker(), ordem.quantidade());
            trava.salvarELiberar(travada);
        } catch (AcoesInsuficientesException erro) {
            trava.liberar(travada);
            publicador.publicar(new AcoesInsuficientes(ordem.ordemId(), ordem.clienteId(), ordem.ticker(),
                    erro.getMessage(), Instant.now()));
            return;
        } catch (RuntimeException erro) {
            trava.liberar(travada);
            throw erro;
        }
        publicador.publicar(new AcoesReservadas(ordem.ordemId(), ordem.clienteId(), ordem.ticker(),
                ordem.quantidade(), Instant.now()));
    }
}
