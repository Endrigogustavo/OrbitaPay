package com.orbitapay.carteira.adapter.out.messaging;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.orbitapay.carteira.adapter.messaging.MensageriaProperties;
import com.orbitapay.carteira.adapter.out.messaging.mensagem.ReservaDeAcoesMensagem;
import com.orbitapay.carteira.application.port.out.PublicadorDeEventosDeCarteira;
import com.orbitapay.carteira.domain.event.AcoesInsuficientes;
import com.orbitapay.carteira.domain.event.AcoesReservadas;
import com.orbitapay.carteira.domain.event.EventoDeCarteira;

@Component
public class RabbitPublicadorDeEventosDeCarteira implements PublicadorDeEventosDeCarteira {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitPublicadorDeEventosDeCarteira.class);

    private final RabbitTemplate rabbitTemplate;
    private final MensageriaProperties mensageria;

    public RabbitPublicadorDeEventosDeCarteira(RabbitTemplate rabbitTemplate, MensageriaProperties mensageria) {
        this.rabbitTemplate = rabbitTemplate;
        this.mensageria = mensageria;
    }

    @Override
    public void publicar(EventoDeCarteira evento) {
        String eventoId = UUID.randomUUID().toString();
        switch (evento) {
            case AcoesReservadas e -> enviar("acoes-reservadas", new ReservaDeAcoesMensagem(eventoId,
                    "ACOES_RESERVADAS", e.ordemId(), e.clienteId(), e.ticker(), e.quantidade(), null,
                    e.quantidade() + " " + e.ticker() + " reservadas para a ordem " + e.ordemId() + ".",
                    e.ocorridoEm()));
            case AcoesInsuficientes e -> enviar("acoes-insuficientes", new ReservaDeAcoesMensagem(eventoId,
                    "ACOES_INSUFICIENTES", e.ordemId(), e.clienteId(), e.ticker(), null, e.motivo(),
                    "Reserva recusada para a ordem " + e.ordemId() + ": " + e.motivo(), e.ocorridoEm()));
        }
    }

    private void enviar(String nomeDoTopico, Object mensagem) {
        String topico = mensageria.topico(nomeDoTopico);
        rabbitTemplate.convertAndSend(mensageria.exchange(), topico, mensagem);
        LOG.info("PUBLICADO [{}] {}", topico, mensagem);
    }
}
