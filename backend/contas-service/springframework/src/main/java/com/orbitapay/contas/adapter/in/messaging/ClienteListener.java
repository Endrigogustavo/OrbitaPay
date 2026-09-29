package com.orbitapay.contas.adapter.in.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.orbitapay.contas.adapter.in.messaging.mensagem.EventoDeClienteMensagem;
import com.orbitapay.contas.application.port.in.AbrirContaUseCase;
import com.orbitapay.contas.application.port.in.SincronizarTitularUseCase;

@Component
public class ClienteListener {

    private static final Logger LOG = LoggerFactory.getLogger(ClienteListener.class);

    private final AbrirContaUseCase abrirConta;
    private final SincronizarTitularUseCase sincronizarTitular;

    public ClienteListener(AbrirContaUseCase abrirConta, SincronizarTitularUseCase sincronizarTitular) {
        this.abrirConta = abrirConta;
        this.sincronizarTitular = sincronizarTitular;
    }

    @RabbitListener(queues = "${mensageria.assinaturas.cliente-eventos.fila}", concurrency = "1")
    public void receber(@Payload EventoDeClienteMensagem mensagem) {
        LOG.info("RECEBIDO {} {}", mensagem.evento(), mensagem);
        switch (mensagem.evento()) {
            case "CLIENTE_CADASTRADO" -> abrirConta.executar(new AbrirContaUseCase.Comando(mensagem.clienteId(),
                    mensagem.nome(), mensagem.depositoInicial()));
            case "CLIENTE_ATUALIZADO" -> sincronizarTitular.atualizarNome(mensagem.clienteId(), mensagem.nome());
            case "CLIENTE_SITUACAO_ALTERADA" -> sincronizarTitular.alterarSituacao(mensagem.clienteId(),
                    mensagem.bloqueado());
            case "CLIENTE_REMOVIDO" -> sincronizarTitular.encerrar(mensagem.clienteId());
            default -> LOG.warn("Evento de cliente ignorado: {}", mensagem.evento());
        }
    }
}
