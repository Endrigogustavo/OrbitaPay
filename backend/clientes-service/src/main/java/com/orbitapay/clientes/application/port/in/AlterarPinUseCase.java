package com.orbitapay.clientes.application.port.in;

public interface AlterarPinUseCase {

    void executar(String clienteId, String pinAtual, String novoPin);
}
