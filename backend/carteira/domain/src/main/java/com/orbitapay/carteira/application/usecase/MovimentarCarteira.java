package com.orbitapay.carteira.application.usecase;

import java.math.BigDecimal;
import java.time.Instant;

import com.orbitapay.carteira.application.service.PublicadorDeEventosDeCarteira;
import com.orbitapay.carteira.domain.event.AcoesInsuficientes;
import com.orbitapay.carteira.domain.event.AcoesReservadas;
import com.orbitapay.carteira.domain.exception.AcoesInsuficientesException;
import com.orbitapay.carteira.domain.repository.CarteiraRepository;
import com.orbitapay.carteira.domain.repository.CarteiraTravada;

public class MovimentarCarteira {

    public record Comando(String ordemId, String clienteId, String ticker, int quantidade, BigDecimal precoUnitario) {
    }

    private final CarteiraRepository repositorio;
    private final PublicadorDeEventosDeCarteira publicador;

    public MovimentarCarteira(CarteiraRepository repositorio, PublicadorDeEventosDeCarteira publicador) {
        this.repositorio = repositorio;
        this.publicador = publicador;
    }

    public void reservarParaVenda(Comando comando) {
        CarteiraTravada travada = repositorio.travarPorCliente(comando.clienteId());
        try {
            travada.carteira().reservarParaVenda(comando.ordemId(), comando.ticker(), comando.quantidade());
            repositorio.salvarELiberar(travada);
        } catch (AcoesInsuficientesException erro) {
            repositorio.liberar(travada);
            publicador.publicar(new AcoesInsuficientes(comando.ordemId(), comando.clienteId(), comando.ticker(),
                    erro.getMessage(), Instant.now()));
            return;
        } catch (RuntimeException erro) {
            repositorio.liberar(travada);
            throw erro;
        }
        publicador.publicar(new AcoesReservadas(comando.ordemId(), comando.clienteId(), comando.ticker(),
                comando.quantidade(), Instant.now()));
    }

    public void liquidarCompra(Comando comando) {
        CarteiraTravada travada = repositorio.travarPorCliente(comando.clienteId());
        try {
            travada.carteira().registrarCompra(comando.ordemId(), comando.ticker(), comando.quantidade(),
                    comando.precoUnitario());
            repositorio.salvarELiberar(travada);
        } catch (RuntimeException erro) {
            repositorio.liberar(travada);
            throw erro;
        }
    }

    public void liquidarVenda(Comando comando) {
        CarteiraTravada travada = repositorio.travarPorCliente(comando.clienteId());
        try {
            travada.carteira().liquidarVenda(comando.ordemId(), comando.ticker());
            repositorio.salvarELiberar(travada);
        } catch (RuntimeException erro) {
            repositorio.liberar(travada);
            throw erro;
        }
    }

    public void cancelarVenda(Comando comando) {
        CarteiraTravada travada = repositorio.travarPorCliente(comando.clienteId());
        try {
            travada.carteira().cancelarReserva(comando.ordemId(), comando.ticker());
            repositorio.salvarELiberar(travada);
        } catch (RuntimeException erro) {
            repositorio.liberar(travada);
            throw erro;
        }
    }

    public void encerrar(String clienteId) {
        repositorio.removerPorCliente(clienteId);
    }
}
