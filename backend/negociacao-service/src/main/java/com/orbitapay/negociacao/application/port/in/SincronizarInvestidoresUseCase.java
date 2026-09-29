package com.orbitapay.negociacao.application.port.in;

public interface SincronizarInvestidoresUseCase {

    void registrar(String clienteId, boolean bloqueado);

    void remover(String clienteId);
}
