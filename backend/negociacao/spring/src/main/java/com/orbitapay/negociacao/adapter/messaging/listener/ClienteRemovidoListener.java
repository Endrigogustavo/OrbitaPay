package com.orbitapay.negociacao.adapter.messaging.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.orbitapay.negociacao.adapter.messaging.dto.ClienteRemovidoMessage;
import com.orbitapay.negociacao.adapter.messaging.CanaisDeMensageria;
import com.orbitapay.negociacao.application.usecase.CarteiraUseCase;

@Component
public class ClienteRemovidoListener {

    private final CarteiraUseCase carteiraUseCase;

    public ClienteRemovidoListener(CarteiraUseCase carteiraUseCase) {
        this.carteiraUseCase = carteiraUseCase;
    }

    @RabbitListener(queues = CanaisDeMensageria.FILA_CLIENTES_REMOVIDOS)
    public void encerrarCarteiraDoClienteRemovido(ClienteRemovidoMessage mensagem) {
        carteiraUseCase.encerrarCarteiraDoCliente(mensagem.clienteId());
    }
}
