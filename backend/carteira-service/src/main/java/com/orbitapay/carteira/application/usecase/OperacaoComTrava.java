package com.orbitapay.carteira.application.usecase;

import java.util.function.Consumer;

import com.orbitapay.carteira.application.port.out.CarteiraRepository;
import com.orbitapay.carteira.application.port.out.CarteiraTravada;
import com.orbitapay.carteira.domain.model.Carteira;

public class OperacaoComTrava {

    private final CarteiraRepository repositorio;

    public OperacaoComTrava(CarteiraRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void executar(String clienteId, Consumer<Carteira> operacao) {
        CarteiraTravada travada = repositorio.travarPorCliente(clienteId);
        try {
            operacao.accept(travada.carteira());
            repositorio.salvarELiberar(travada);
        } catch (RuntimeException e) {
            repositorio.liberar(travada);
            throw e;
        }
    }
}
