package com.orbitapay.contas.application.usecase;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;

import com.orbitapay.contas.application.service.GeradorDeNumeroDeConta;
import com.orbitapay.contas.domain.model.Conta;
import com.orbitapay.contas.domain.model.Dinheiro;
import com.orbitapay.contas.domain.repository.ContaRepository;

public class AbrirConta {

    public record Comando(String clienteId, String nomeTitular, BigDecimal depositoInicial) {
    }

    private final ContaRepository repositorio;
    private final GeradorDeNumeroDeConta geradorDeNumero;
    private final Clock relogio;

    public AbrirConta(ContaRepository repositorio, GeradorDeNumeroDeConta geradorDeNumero, Clock relogio) {
        this.repositorio = repositorio;
        this.geradorDeNumero = geradorDeNumero;
        this.relogio = relogio;
    }

    public void executar(Comando comando) {
        if (repositorio.existePorCliente(comando.clienteId())) {
            return;
        }
        Conta conta = Conta.abrir(repositorio.proximoId(), comando.clienteId(), comando.nomeTitular(),
                geradorDeNumero.proximo(), new Dinheiro(comando.depositoInicial()), Instant.now(relogio));
        repositorio.inserir(conta);
    }
}
