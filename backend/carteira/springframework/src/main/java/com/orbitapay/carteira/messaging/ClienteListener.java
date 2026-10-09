package com.orbitapay.carteira.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.orbitapay.carteira.application.usecase.EncerrarCarteira;
import com.orbitapay.carteira.messaging.mensagem.EventoDeClienteMensagem;

@Component
public class ClienteListener {

    private static final Logger LOG = LoggerFactory.getLogger(ClienteListener.class);

    private final EncerrarCarteira encerrarCarteira;

    public ClienteListener(EncerrarCarteira encerrarCarteira) {
        this.encerrarCarteira = encerrarCarteira;
    }

    @RabbitListener(queues = "${mensageria.assinaturas.cliente-eventos.fila}", concurrency = "1")
    public void receber(@Payload EventoDeClienteMensagem mensagem) {
        if ("CLIENTE_REMOVIDO".equals(mensagem.evento())) {
            LOG.info("RECEBIDO {} {}", mensagem.evento(), mensagem.clienteId());
            encerrarCarteira.executar(mensagem.clienteId());
        }
    }
}
