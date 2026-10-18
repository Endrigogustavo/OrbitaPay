package com.orbitapay.negociacao.adapter.messaging.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.orbitapay.negociacao.adapter.messaging.CanaisDeMensageria;
import com.orbitapay.negociacao.adapter.messaging.dto.OrdemSolicitadaMessage;
import com.orbitapay.negociacao.application.usecase.OrdemUseCase;

@Component
public class OrdemSolicitadaListener {

    private final OrdemUseCase ordemUseCase;

    public OrdemSolicitadaListener(OrdemUseCase ordemUseCase) {
        this.ordemUseCase = ordemUseCase;
    }

    @RabbitListener(queues = CanaisDeMensageria.FILA_ORDENS_SOLICITADAS)
    public void executarOrdemSolicitadaPeloCliente(OrdemSolicitadaMessage mensagem) {
        ordemUseCase.executarOrdemSolicitada(mensagem.ordemId());
    }
}
