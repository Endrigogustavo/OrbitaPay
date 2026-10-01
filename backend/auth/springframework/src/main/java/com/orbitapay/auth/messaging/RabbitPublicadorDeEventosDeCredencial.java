package com.orbitapay.auth.messaging;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.orbitapay.auth.application.service.PublicadorDeEventosDeCredencial;
import com.orbitapay.auth.domain.event.CredencialBloqueadaPorPin;
import com.orbitapay.auth.domain.event.EventoDeCredencial;
import com.orbitapay.auth.messaging.mensagem.CredencialBloqueadaPorPinMensagem;

@Component
public class RabbitPublicadorDeEventosDeCredencial implements PublicadorDeEventosDeCredencial {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitPublicadorDeEventosDeCredencial.class);

    private final RabbitTemplate rabbitTemplate;
    private final MensageriaProperties mensageria;

    public RabbitPublicadorDeEventosDeCredencial(RabbitTemplate rabbitTemplate, MensageriaProperties mensageria) {
        this.rabbitTemplate = rabbitTemplate;
        this.mensageria = mensageria;
    }

    @Override
    public void publicar(EventoDeCredencial evento) {
        String eventoId = UUID.randomUUID().toString();
        switch (evento) {
            case CredencialBloqueadaPorPin e -> enviar("bloqueada-por-pin", new CredencialBloqueadaPorPinMensagem(
                    eventoId, "CREDENCIAL_BLOQUEADA_POR_PIN", e.clienteId(), e.tentativas(),
                    "Cliente " + e.clienteId() + " errou o PIN " + e.tentativas() + " vezes seguidas.",
                    e.ocorridoEm()));
        }
    }

    private void enviar(String nomeDoTopico, Object mensagem) {
        String topico = mensageria.topico(nomeDoTopico);
        rabbitTemplate.convertAndSend(mensageria.exchange(), topico, mensagem);
        LOG.info("PUBLICADO [{}] {}", topico, mensagem);
    }
}
