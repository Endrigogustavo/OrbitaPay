package com.orbitapay.contas.application.usecase;

import java.time.Clock;
import java.time.Instant;

import com.orbitapay.contas.application.port.in.AbrirContaUseCase;
import com.orbitapay.contas.application.port.out.ContaRepository;
import com.orbitapay.contas.application.port.out.GeradorDeNumeroDeConta;
import com.orbitapay.contas.domain.model.Conta;
import com.orbitapay.contas.domain.model.Dinheiro;

public class AbrirConta implements AbrirContaUseCase {

    private final ContaRepository repositorio;
    private final GeradorDeNumeroDeConta geradorDeNumero;
    private final Clock relogio;

    public AbrirConta(ContaRepository repositorio, GeradorDeNumeroDeConta geradorDeNumero, Clock relogio) {
        this.repositorio = repositorio;
        this.geradorDeNumero = geradorDeNumero;
        this.relogio = relogio;
    }

    @Override
    public void executar(Comando comando) {
        if (repositorio.existePorCliente(comando.clienteId())) {
            return;
        }
        Conta conta = Conta.abrir(repositorio.proximoId(), comando.clienteId(), comando.nomeTitular(),
                geradorDeNumero.proximo(), new Dinheiro(comando.depositoInicial()), Instant.now(relogio));
        repositorio.inserir(conta);
    }
}
