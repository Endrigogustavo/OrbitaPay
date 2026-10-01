package com.orbitapay.contas.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.orbitapay.contas.application.usecase.AbrirConta;
import com.orbitapay.contas.application.usecase.SincronizarTitular;
import com.orbitapay.contas.messaging.mensagem.EventoDeClienteMensagem;

@Component
public class ClienteListener {

    private static final Logger LOG = LoggerFactory.getLogger(ClienteListener.class);

    private final AbrirConta abrirConta;
    private final SincronizarTitular sincronizarTitular;

    public ClienteListener(AbrirConta abrirConta, SincronizarTitular sincronizarTitular) {
        this.abrirConta = abrirConta;
        this.sincronizarTitular = sincronizarTitular;
    }

    @RabbitListener(queues = "${mensageria.assinaturas.cliente-eventos.fila}", concurrency = "1")
    public void receber(@Payload EventoDeClienteMensagem mensagem) {
        LOG.info("RECEBIDO {} {}", mensagem.evento(), mensagem);
        switch (mensagem.evento()) {
            case "CLIENTE_CADASTRADO" -> abrirConta.executar(new AbrirConta.Comando(mensagem.clienteId(),
                    mensagem.nome(), mensagem.depositoInicial()));
            case "CLIENTE_ATUALIZADO" -> sincronizarTitular.atualizarNome(mensagem.clienteId(), mensagem.nome());
            case "CLIENTE_SITUACAO_ALTERADA" -> sincronizarTitular.alterarSituacao(mensagem.clienteId(),
                    mensagem.bloqueado());
            case "CLIENTE_REMOVIDO" -> sincronizarTitular.encerrar(mensagem.clienteId());
            default -> LOG.warn("Evento de cliente ignorado: {}", mensagem.evento());
        }
    }
}
