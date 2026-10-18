package com.orbitapay.auth.infrastructure.messaging;

import static com.orbitapay.auth.adapter.messaging.CanaisDeMensageria.EVENTO_CLIENTE_REMOVIDO;
import static com.orbitapay.auth.adapter.messaging.CanaisDeMensageria.EXCHANGE_DE_EVENTOS;
import static com.orbitapay.auth.adapter.messaging.CanaisDeMensageria.FILA_CLIENTES_REMOVIDOS;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public TopicExchange exchangeDeEventos() {
        return new TopicExchange(EXCHANGE_DE_EVENTOS);
    }

    @Bean
    public Queue filaDeClientesRemovidos() {
        return QueueBuilder.durable(FILA_CLIENTES_REMOVIDOS)
                .deadLetterExchange("")
                .deadLetterRoutingKey(FILA_CLIENTES_REMOVIDOS + ".erro")
                .build();
    }

    @Bean
    public Queue filaDeClientesRemovidosComErro() {
        return QueueBuilder.durable(FILA_CLIENTES_REMOVIDOS + ".erro").build();
    }

    @Bean
    public Binding ligarFilaAoEventoDeClienteRemovido(Queue filaDeClientesRemovidos, TopicExchange exchangeDeEventos) {
        return BindingBuilder.bind(filaDeClientesRemovidos).to(exchangeDeEventos).with(EVENTO_CLIENTE_REMOVIDO);
    }

    @Bean
    public MessageConverter conversorDeMensagensJson() {
        return new JacksonJsonMessageConverter();
    }
}
