package com.orbitapay.clientes.adapter.messaging.publisher;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.orbitapay.clientes.adapter.messaging.dto.ClienteRemovidoMessage;
import com.orbitapay.clientes.application.gateway.IEventoGateway;
import com.orbitapay.clientes.adapter.messaging.CanaisDeMensageria;

@Component
public class EventoRabbitMqPublisher implements IEventoGateway {

    private final RabbitTemplate rabbitTemplate;

    public EventoRabbitMqPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publicarClienteRemovido(String clienteId) {
        rabbitTemplate.convertAndSend(CanaisDeMensageria.EXCHANGE_DE_EVENTOS, CanaisDeMensageria.EVENTO_CLIENTE_REMOVIDO,
                new ClienteRemovidoMessage(clienteId));
    }
}
