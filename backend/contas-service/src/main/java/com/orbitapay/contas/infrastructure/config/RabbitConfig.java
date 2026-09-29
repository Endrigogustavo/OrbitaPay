package com.orbitapay.contas.infrastructure.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.orbitapay.contas.adapter.messaging.MensageriaProperties;

@Configuration
public class RabbitConfig {

    @Bean
    public Declarables topologia(MensageriaProperties mensageria) {
        TopicExchange eventos = new TopicExchange(mensageria.exchange(), true, false);
        TopicExchange mortas = new TopicExchange(mensageria.exchangeMorta(), true, false);
        Queue filaMorta = QueueBuilder.durable(mensageria.filaMorta()).build();
        List<Declarable> declaracoes = new ArrayList<>(List.of(eventos, mortas, filaMorta,
                BindingBuilder.bind(filaMorta).to(mortas).with(mensageria.chaveMorta())));
        mensageria.assinaturas().values().forEach(assinatura -> {
            Queue fila = QueueBuilder.durable(assinatura.fila())
                    .deadLetterExchange(mensageria.exchangeMorta())
                    .deadLetterRoutingKey(assinatura.fila())
                    .build();
            declaracoes.add(fila);
            declaracoes.add(BindingBuilder.bind(fila).to(eventos).with(assinatura.topico()));
        });
        return new Declarables(declaracoes);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
}
