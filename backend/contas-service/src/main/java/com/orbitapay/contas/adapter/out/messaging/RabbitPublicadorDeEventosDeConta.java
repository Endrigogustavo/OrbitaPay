package com.orbitapay.contas.adapter.out.messaging;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.orbitapay.contas.adapter.messaging.MensageriaProperties;
import com.orbitapay.contas.adapter.out.messaging.mensagem.DebitoDeCompraAprovadoMensagem;
import com.orbitapay.contas.adapter.out.messaging.mensagem.DebitoDeCompraRecusadoMensagem;
import com.orbitapay.contas.application.port.out.PublicadorDeEventosDeConta;
import com.orbitapay.contas.domain.event.DebitoDeCompraAprovado;
import com.orbitapay.contas.domain.event.DebitoDeCompraRecusado;
import com.orbitapay.contas.domain.event.EventoDeConta;

@Component
public class RabbitPublicadorDeEventosDeConta implements PublicadorDeEventosDeConta {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitPublicadorDeEventosDeConta.class);

    private final RabbitTemplate rabbitTemplate;
    private final MensageriaProperties mensageria;

    public RabbitPublicadorDeEventosDeConta(RabbitTemplate rabbitTemplate, MensageriaProperties mensageria) {
        this.rabbitTemplate = rabbitTemplate;
        this.mensageria = mensageria;
    }

    @Override
    public void publicar(EventoDeConta evento) {
        String eventoId = UUID.randomUUID().toString();
        switch (evento) {
            case DebitoDeCompraAprovado e -> enviar("debito-aprovado", new DebitoDeCompraAprovadoMensagem(eventoId,
                    "DEBITO_DE_COMPRA_APROVADO", e.ordemId(), e.clienteId(), e.valor().valor(),
                    e.saldoRestante().valor(),
                    "Débito de " + e.valor().formatado() + " aprovado para a ordem " + e.ordemId() + ".",
                    e.ocorridoEm()));
            case DebitoDeCompraRecusado e -> enviar("debito-recusado", new DebitoDeCompraRecusadoMensagem(eventoId,
                    "DEBITO_DE_COMPRA_RECUSADO", e.ordemId(), e.clienteId(), e.motivo(),
                    "Débito recusado para a ordem " + e.ordemId() + ": " + e.motivo(), e.ocorridoEm()));
        }
    }

    private void enviar(String nomeDoTopico, Object mensagem) {
        String topico = mensageria.topico(nomeDoTopico);
        rabbitTemplate.convertAndSend(mensageria.exchange(), topico, mensagem);
        LOG.info("PUBLICADO [{}] {}", topico, mensagem);
    }
}
