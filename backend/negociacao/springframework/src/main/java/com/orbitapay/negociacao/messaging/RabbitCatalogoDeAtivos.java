package com.orbitapay.negociacao.messaging;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import com.orbitapay.negociacao.application.dto.AtivoDoCatalogo;
import com.orbitapay.negociacao.application.service.CatalogoDeAtivos;
import com.orbitapay.negociacao.messaging.mensagem.AtivoConsultadoMensagem;
import com.orbitapay.negociacao.messaging.mensagem.ConsultaDeAtivoMensagem;

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
    public Optional<AtivoDoCatalogo> consultar(String ticker) {
        LOG.info("CONSULTANDO contexto de Ativos via mensageria: {}", ticker);
        try {
            AtivoConsultadoMensagem resposta = rabbitTemplate.convertSendAndReceiveAsType("",
                    mensageria.filaDeConsultasDeAtivos(), new ConsultaDeAtivoMensagem(ticker, "negociacao"),
                    new ParameterizedTypeReference<AtivoConsultadoMensagem>() {
                    });
            if (resposta == null || !resposta.encontrado()) {
                return Optional.empty();
            }
            return Optional.of(new AtivoDoCatalogo(resposta.ticker(), resposta.nome(), resposta.bolsa(),
                    resposta.moeda(), resposta.cambio(), resposta.cotacao(), resposta.quantidadeEmitida()));
        } catch (AmqpException e) {
            LOG.warn("Contexto de Ativos indisponível para consulta de {}: {}", ticker, e.getMessage());
            return Optional.empty();
        }
    }
}
