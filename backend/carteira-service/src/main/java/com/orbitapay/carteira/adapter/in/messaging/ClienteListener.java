package com.orbitapay.carteira.adapter.in.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.orbitapay.carteira.adapter.in.messaging.mensagem.EventoDeClienteMensagem;
import com.orbitapay.carteira.application.port.in.MovimentarCarteiraUseCase;

@Component
public class ClienteListener {

    private static final Logger LOG = LoggerFactory.getLogger(ClienteListener.class);

    private final MovimentarCarteiraUseCase movimentarCarteira;

    public ClienteListener(MovimentarCarteiraUseCase movimentarCarteira) {
        this.movimentarCarteira = movimentarCarteira;
    }

    @RabbitListener(queues = "${mensageria.assinaturas.cliente-eventos.fila}", concurrency = "1")
    public void receber(@Payload EventoDeClienteMensagem mensagem) {
        if ("CLIENTE_REMOVIDO".equals(mensagem.evento())) {
            LOG.info("RECEBIDO {} {}", mensagem.evento(), mensagem.clienteId());
            movimentarCarteira.encerrar(mensagem.clienteId());
        }
    }
}
