package com.orbitapay.contas.application.usecase;

import java.util.function.Function;

import com.orbitapay.contas.application.port.out.ContaRepository;
import com.orbitapay.contas.application.port.out.ContaTravada;
import com.orbitapay.contas.domain.model.Conta;

public class OperacaoComTrava {

    private final ContaRepository repositorio;

    public OperacaoComTrava(ContaRepository repositorio) {
        this.repositorio = repositorio;
    }

    public <T> T executar(String clienteId, Function<Conta, T> operacao) {
        ContaTravada travada = repositorio.travarPorCliente(clienteId);
        try {
            T resultado = operacao.apply(travada.conta());
            repositorio.salvarELiberar(travada);
            return resultado;
        } catch (RuntimeException e) {
            repositorio.liberar(travada);
            throw e;
        }
    }
}
