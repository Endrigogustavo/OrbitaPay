package com.orbitapay.auth.adapter.messaging.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.orbitapay.auth.adapter.messaging.dto.ClienteRemovidoMessage;
import com.orbitapay.auth.adapter.messaging.CanaisDeMensageria;
import com.orbitapay.auth.application.usecase.CredencialUseCase;

@Component
public class ClienteRemovidoListener {

    private final CredencialUseCase credencialUseCase;

    public ClienteRemovidoListener(CredencialUseCase credencialUseCase) {
        this.credencialUseCase = credencialUseCase;
    }

    @RabbitListener(queues = CanaisDeMensageria.FILA_CLIENTES_REMOVIDOS)
    public void removerCredencialDoClienteRemovido(ClienteRemovidoMessage mensagem) {
        credencialUseCase.removerCredencialESessoesDoCliente(mensagem.clienteId());
    }
}
