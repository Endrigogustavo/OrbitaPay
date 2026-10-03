package com.orbitapay.contas.application.usecase;

import java.math.BigDecimal;
import java.time.Instant;

import com.orbitapay.contas.domain.model.Dinheiro;
import com.orbitapay.contas.domain.repository.ContaRepository;
import com.orbitapay.contas.domain.repository.ContaTravada;

public class CreditarDeposito {

    public record Comando(String pagamentoId, String clienteId, BigDecimal valor, String metodo) {
    }

    private final ContaRepository repositorio;

    public CreditarDeposito(ContaRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void executar(Comando comando) {
        ContaTravada travada = repositorio.travarPorCliente(comando.clienteId());
        try {
            travada.conta().creditarDeposito(comando.pagamentoId(), new Dinheiro(comando.valor()), comando.metodo(),
                    Instant.now());
            repositorio.salvarELiberar(travada);
        } catch (RuntimeException erro) {
            repositorio.liberar(travada);
            throw erro;
        }
    }
}
