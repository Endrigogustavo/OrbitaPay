package com.orbitapay.clientes.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.orbitapay.clientes.application.usecase.BloquearCliente;
import com.orbitapay.clientes.messaging.mensagem.EventoDeCredencialMensagem;

@Component
public class CredencialListener {

    private static final Logger LOG = LoggerFactory.getLogger(CredencialListener.class);

    private final BloquearCliente bloquearCliente;

    public CredencialListener(BloquearCliente bloquearCliente) {
        this.bloquearCliente = bloquearCliente;
    }

    @RabbitListener(queues = "${mensageria.assinaturas.credencial-eventos.fila}", concurrency = "1")
    public void receber(@Payload EventoDeCredencialMensagem mensagem) {
        LOG.info("RECEBIDO {} {}", mensagem.evento(), mensagem.clienteId());
        if ("CREDENCIAL_BLOQUEADA_POR_PIN".equals(mensagem.evento())) {
            bloquearCliente.porExcessoDeTentativasDePin(mensagem.clienteId());
        }
    }
}
