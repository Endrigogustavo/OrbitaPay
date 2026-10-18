package com.orbitapay.negociacao.infrastructure.messaging;

import static com.orbitapay.negociacao.adapter.messaging.CanaisDeMensageria.EVENTO_CLIENTE_REMOVIDO;
import static com.orbitapay.negociacao.adapter.messaging.CanaisDeMensageria.EVENTO_ORDEM_SOLICITADA;
import static com.orbitapay.negociacao.adapter.messaging.CanaisDeMensageria.EXCHANGE_DE_EVENTOS;
import static com.orbitapay.negociacao.adapter.messaging.CanaisDeMensageria.FILA_CLIENTES_REMOVIDOS;
import static com.orbitapay.negociacao.adapter.messaging.CanaisDeMensageria.FILA_ORDENS_SOLICITADAS;

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
        return criarFilaComFilaDeErro(FILA_CLIENTES_REMOVIDOS);
    }

    @Bean
    public Queue filaDeClientesRemovidosComErro() {
        return QueueBuilder.durable(FILA_CLIENTES_REMOVIDOS + ".erro").build();
    }

    @Bean
    public Queue filaDeOrdensSolicitadas() {
        return criarFilaComFilaDeErro(FILA_ORDENS_SOLICITADAS);
    }

    @Bean
    public Queue filaDeOrdensSolicitadasComErro() {
        return QueueBuilder.durable(FILA_ORDENS_SOLICITADAS + ".erro").build();
    }

    @Bean
    public Binding ligarFilaAoEventoDeClienteRemovido(Queue filaDeClientesRemovidos, TopicExchange exchangeDeEventos) {
        return BindingBuilder.bind(filaDeClientesRemovidos).to(exchangeDeEventos).with(EVENTO_CLIENTE_REMOVIDO);
    }

    @Bean
    public Binding ligarFilaAoEventoDeOrdemSolicitada(Queue filaDeOrdensSolicitadas, TopicExchange exchangeDeEventos) {
        return BindingBuilder.bind(filaDeOrdensSolicitadas).to(exchangeDeEventos).with(EVENTO_ORDEM_SOLICITADA);
    }

    @Bean
    public MessageConverter conversorDeMensagensJson() {
        return new JacksonJsonMessageConverter();
    }

    private static Queue criarFilaComFilaDeErro(String nomeDaFila) {
        return QueueBuilder.durable(nomeDaFila)
                .deadLetterExchange("")
                .deadLetterRoutingKey(nomeDaFila + ".erro")
                .build();
    }
}
