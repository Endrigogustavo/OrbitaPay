package com.orbitapay.carteira.adapter.out.messaging;

import java.math.BigDecimal;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.orbitapay.carteira.adapter.messaging.MensageriaProperties;
import com.orbitapay.carteira.application.port.out.CatalogoDeAtivos;
import com.orbitapay.carteira.domain.model.AtivoCotado;

@Component
public class RabbitCatalogoDeAtivos implements CatalogoDeAtivos {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitCatalogoDeAtivos.class);

    private final RabbitTemplate rabbitTemplate;
    private final MensageriaProperties mensageria;

    public RabbitCatalogoDeAtivos(RabbitTemplate rabbitTemplate, MensageriaProperties mensageria) {
        this.rabbitTemplate = rabbitTemplate;
        this.mensageria = mensageria;
    }

    @Override
    public Optional<AtivoCotado> consultar(String ticker) {
        LOG.info("CONSULTANDO contexto de Ativos via mensageria: {}", ticker);
        try {
            Resposta resposta = rabbitTemplate.convertSendAndReceiveAsType("", mensageria.filaDeConsultasDeAtivos(),
                    new Consulta(ticker, "carteira"), new ParameterizedTypeReference<Resposta>() {
                    });
            if (resposta == null || !resposta.encontrado()) {
                return Optional.empty();
            }
            return Optional.of(new AtivoCotado(resposta.ticker(), resposta.nome(), resposta.moeda(), resposta.cambio(),
                    resposta.cotacao()));
        } catch (AmqpException e) {
            LOG.warn("Contexto de Ativos indisponível para consulta de {}: {}", ticker, e.getMessage());
            return Optional.empty();
        }
    }

    public record Consulta(String ticker, String solicitante) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Resposta(boolean encontrado, String ticker, String nome, String moeda, BigDecimal cambio,
            BigDecimal cotacao) {
    }
}
