package com.orbitapay.negociacao.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.orbitapay.negociacao.application.usecase.RegistrarInvestidor;
import com.orbitapay.negociacao.application.usecase.RemoverInvestidor;
import com.orbitapay.negociacao.messaging.mensagem.EventoDeClienteMensagem;

@Component
public class ClienteListener {

    private static final Logger LOG = LoggerFactory.getLogger(ClienteListener.class);

    private final RegistrarInvestidor registrarInvestidor;
    private final RemoverInvestidor removerInvestidor;

    public ClienteListener(RegistrarInvestidor registrarInvestidor, RemoverInvestidor removerInvestidor) {
        this.registrarInvestidor = registrarInvestidor;
        this.removerInvestidor = removerInvestidor;
    }

    @RabbitListener(queues = "${mensageria.assinaturas.cliente-eventos.fila}", concurrency = "1")
    public void receber(@Payload EventoDeClienteMensagem mensagem) {
        LOG.info("RECEBIDO {} {}", mensagem.evento(), mensagem);
        switch (mensagem.evento()) {
            case "CLIENTE_CADASTRADO", "CLIENTE_SITUACAO_ALTERADA" ->
                registrarInvestidor.executar(mensagem.clienteId(), mensagem.bloqueado());
            case "CLIENTE_REMOVIDO" -> removerInvestidor.executar(mensagem.clienteId());
            default -> LOG.debug("Evento de cliente sem interesse para a negociação: {}", mensagem.evento());
        }
    }
}
