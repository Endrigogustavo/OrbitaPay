package com.orbitapay.pagamentos.infrastructure.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.orbitapay.pagamentos.messaging.MensageriaProperties;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RabbitConfig {

    @Bean
    public TopicExchange orbitaExchange(MensageriaProperties mensageria) {
        return new TopicExchange(mensageria.exchange(), true, false);
    }

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
        return rabbitTemplate;
    }
}
