package com.orbitapay.clientes.infrastructure.messaging;

import static com.orbitapay.clientes.adapter.messaging.CanaisDeMensageria.EVENTO_PAGAMENTO_CONFIRMADO;
import static com.orbitapay.clientes.adapter.messaging.CanaisDeMensageria.EXCHANGE_DE_EVENTOS;
import static com.orbitapay.clientes.adapter.messaging.CanaisDeMensageria.FILA_PAGAMENTOS_CONFIRMADOS;

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
    public Queue filaDePagamentosConfirmados() {
        return QueueBuilder.durable(FILA_PAGAMENTOS_CONFIRMADOS)
                .deadLetterExchange("")
                .deadLetterRoutingKey(FILA_PAGAMENTOS_CONFIRMADOS + ".erro")
                .build();
    }

    @Bean
    public Queue filaDePagamentosConfirmadosComErro() {
        return QueueBuilder.durable(FILA_PAGAMENTOS_CONFIRMADOS + ".erro").build();
    }

    @Bean
    public Binding ligarFilaAoEventoDePagamentoConfirmado(Queue filaDePagamentosConfirmados,
            TopicExchange exchangeDeEventos) {
        return BindingBuilder.bind(filaDePagamentosConfirmados).to(exchangeDeEventos).with(EVENTO_PAGAMENTO_CONFIRMADO);
    }

    @Bean
    public MessageConverter conversorDeMensagensJson() {
        return new JacksonJsonMessageConverter();
    }
}
