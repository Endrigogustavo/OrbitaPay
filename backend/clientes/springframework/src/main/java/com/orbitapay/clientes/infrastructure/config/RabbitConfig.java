package com.orbitapay.clientes.infrastructure.config;

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

import com.orbitapay.clientes.messaging.MensageriaProperties;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.json.JsonMapper;

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

    /**
     * Leitor tolerante: campos desconhecidos são ignorados e campos ausentes assumem o valor padrão. Sem isso, o
     * Jackson 3 recusa, por exemplo, um {@code cliente.cadastrado} lido como um record com {@code boolean bloqueado}.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        JsonMapper leitorTolerante = JsonMapper.builder()
                .findAndAddModules(RabbitConfig.class.getClassLoader())
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
                .build();
        return new JacksonJsonMessageConverter(leitorTolerante, "*");
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        rabbitTemplate.setReplyTimeout(5000);
        return rabbitTemplate;
    }
}
