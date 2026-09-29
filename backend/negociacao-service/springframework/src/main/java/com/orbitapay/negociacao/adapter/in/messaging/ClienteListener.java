package com.orbitapay.negociacao.adapter.in.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.orbitapay.negociacao.adapter.in.messaging.mensagem.EventoDeClienteMensagem;
import com.orbitapay.negociacao.application.port.in.SincronizarInvestidoresUseCase;

@Component
public class ClienteListener {

    private static final Logger LOG = LoggerFactory.getLogger(ClienteListener.class);

    private final SincronizarInvestidoresUseCase sincronizarInvestidores;

    public ClienteListener(SincronizarInvestidoresUseCase sincronizarInvestidores) {
        this.sincronizarInvestidores = sincronizarInvestidores;
    }

    @RabbitListener(queues = "${mensageria.assinaturas.cliente-eventos.fila}", concurrency = "1")
    public void receber(@Payload EventoDeClienteMensagem mensagem) {
        LOG.info("RECEBIDO {} {}", mensagem.evento(), mensagem);
        switch (mensagem.evento()) {
            case "CLIENTE_CADASTRADO", "CLIENTE_SITUACAO_ALTERADA" ->
                sincronizarInvestidores.registrar(mensagem.clienteId(), mensagem.bloqueado());
            case "CLIENTE_REMOVIDO" -> sincronizarInvestidores.remover(mensagem.clienteId());
            default -> LOG.debug("Evento de cliente sem interesse para a negociação: {}", mensagem.evento());
        }
    }
}
