package com.orbitapay.contas.application.usecase;

import java.math.BigDecimal;
import java.time.Instant;

import com.orbitapay.contas.application.dto.Comprovante;
import com.orbitapay.contas.domain.model.Conta;
import com.orbitapay.contas.domain.model.Dinheiro;
import com.orbitapay.contas.domain.model.Lancamento;
import com.orbitapay.contas.domain.repository.ContaRepository;
import com.orbitapay.contas.domain.repository.ContaTravada;

public class Sacar {

    private final ContaRepository repositorio;

    public Sacar(ContaRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Comprovante executar(String clienteId, BigDecimal valor) {
        ContaTravada travada = repositorio.travarPorCliente(clienteId);
        try {
            Conta conta = travada.conta();
            Lancamento saque = conta.sacar(new Dinheiro(valor), Instant.now());
            repositorio.salvarELiberar(travada);
            return new Comprovante(conta, saque);
        } catch (RuntimeException erro) {
            repositorio.liberar(travada);
            throw erro;
        }
    }
}
