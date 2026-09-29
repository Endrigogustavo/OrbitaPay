package com.orbitapay.contas.application.port.in;

public interface SincronizarTitularUseCase {

    void atualizarNome(String clienteId, String nome);

    void alterarSituacao(String clienteId, boolean bloqueado);

    void encerrar(String clienteId);
}
