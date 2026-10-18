package com.orbitapay.negociacao.adapter.messaging.publisher;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.orbitapay.negociacao.adapter.messaging.CanaisDeMensageria;
import com.orbitapay.negociacao.adapter.messaging.dto.OrdemSolicitadaMessage;
import com.orbitapay.negociacao.application.gateway.IEventoGateway;
import com.orbitapay.negociacao.entity.Ordem;

@Component
public class EventoRabbitMqPublisher implements IEventoGateway {

    private final RabbitTemplate rabbitTemplate;

    public EventoRabbitMqPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publicarOrdemSolicitada(Ordem ordem) {
        rabbitTemplate.convertAndSend(CanaisDeMensageria.EXCHANGE_DE_EVENTOS,
                CanaisDeMensageria.EVENTO_ORDEM_SOLICITADA, new OrdemSolicitadaMessage(ordem.id()));
    }
}
