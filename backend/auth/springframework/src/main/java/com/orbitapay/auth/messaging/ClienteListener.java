package com.orbitapay.auth.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.orbitapay.auth.application.usecase.AtualizarEmailDaCredencial;
import com.orbitapay.auth.application.usecase.RemoverCredencial;
import com.orbitapay.auth.application.usecase.ZerarTentativasDePin;
import com.orbitapay.auth.messaging.mensagem.EventoDeClienteMensagem;

@Component
public class ClienteListener {

    private static final Logger LOG = LoggerFactory.getLogger(ClienteListener.class);

    private final AtualizarEmailDaCredencial atualizarEmailDaCredencial;
    private final ZerarTentativasDePin zerarTentativasDePin;
    private final RemoverCredencial removerCredencial;

    public ClienteListener(AtualizarEmailDaCredencial atualizarEmailDaCredencial,
            ZerarTentativasDePin zerarTentativasDePin, RemoverCredencial removerCredencial) {
        this.atualizarEmailDaCredencial = atualizarEmailDaCredencial;
        this.zerarTentativasDePin = zerarTentativasDePin;
        this.removerCredencial = removerCredencial;
    }

    @RabbitListener(queues = "${mensageria.assinaturas.cliente-eventos.fila}", concurrency = "1")
    public void receber(@Payload EventoDeClienteMensagem mensagem) {
        LOG.info("RECEBIDO {} {}", mensagem.evento(), mensagem.clienteId());
        switch (mensagem.evento()) {
            case "CLIENTE_ATUALIZADO" -> atualizarEmailDaCredencial.executar(mensagem.clienteId(), mensagem.email());
            case "CLIENTE_SITUACAO_ALTERADA" -> {
                if (!mensagem.bloqueado()) {
                    zerarTentativasDePin.executar(mensagem.clienteId());
                }
            }
            case "CLIENTE_REMOVIDO" -> removerCredencial.executar(mensagem.clienteId());
            default -> LOG.debug("Evento de cliente sem interesse para a autenticação: {}", mensagem.evento());
        }
    }
}
