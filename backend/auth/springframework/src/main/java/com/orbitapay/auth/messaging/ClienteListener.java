package com.orbitapay.auth.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.orbitapay.auth.application.usecase.SincronizarCliente;
import com.orbitapay.auth.messaging.mensagem.EventoDeClienteMensagem;

@Component
public class ClienteListener {

    private static final Logger LOG = LoggerFactory.getLogger(ClienteListener.class);

    private final SincronizarCliente sincronizarCliente;

    public ClienteListener(SincronizarCliente sincronizarCliente) {
        this.sincronizarCliente = sincronizarCliente;
    }

    @RabbitListener(queues = "${mensageria.assinaturas.cliente-eventos.fila}", concurrency = "1")
    public void receber(@Payload EventoDeClienteMensagem mensagem) {
        LOG.info("RECEBIDO {} {}", mensagem.evento(), mensagem.clienteId());
        switch (mensagem.evento()) {
            case "CLIENTE_ATUALIZADO" -> sincronizarCliente.atualizarEmail(mensagem.clienteId(), mensagem.email());
            case "CLIENTE_SITUACAO_ALTERADA" -> {
                if (!mensagem.bloqueado()) {
                    sincronizarCliente.desbloqueado(mensagem.clienteId());
                }
            }
            case "CLIENTE_REMOVIDO" -> sincronizarCliente.encerrar(mensagem.clienteId());
            default -> LOG.debug("Evento de cliente sem interesse para a autenticação: {}", mensagem.evento());
        }
    }
}
