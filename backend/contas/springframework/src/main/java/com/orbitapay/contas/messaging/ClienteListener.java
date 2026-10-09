package com.orbitapay.contas.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.orbitapay.contas.application.usecase.AbrirConta;
import com.orbitapay.contas.application.usecase.AlterarSituacaoDoTitular;
import com.orbitapay.contas.application.usecase.AtualizarNomeDoTitular;
import com.orbitapay.contas.application.usecase.EncerrarConta;
import com.orbitapay.contas.messaging.mensagem.EventoDeClienteMensagem;

@Component
public class ClienteListener {

    private static final Logger LOG = LoggerFactory.getLogger(ClienteListener.class);

    private final AbrirConta abrirConta;
    private final AtualizarNomeDoTitular atualizarNomeDoTitular;
    private final AlterarSituacaoDoTitular alterarSituacaoDoTitular;
    private final EncerrarConta encerrarConta;

    public ClienteListener(AbrirConta abrirConta, AtualizarNomeDoTitular atualizarNomeDoTitular,
            AlterarSituacaoDoTitular alterarSituacaoDoTitular, EncerrarConta encerrarConta) {
        this.abrirConta = abrirConta;
        this.atualizarNomeDoTitular = atualizarNomeDoTitular;
        this.alterarSituacaoDoTitular = alterarSituacaoDoTitular;
        this.encerrarConta = encerrarConta;
    }

    @RabbitListener(queues = "${mensageria.assinaturas.cliente-eventos.fila}", concurrency = "1")
    public void receber(@Payload EventoDeClienteMensagem mensagem) {
        LOG.info("RECEBIDO {} {}", mensagem.evento(), mensagem);
        switch (mensagem.evento()) {
            case "CLIENTE_CADASTRADO" -> abrirConta.executar(new AbrirConta.Comando(mensagem.clienteId(),
                    mensagem.nome(), mensagem.depositoInicial()));
            case "CLIENTE_ATUALIZADO" -> atualizarNomeDoTitular.executar(mensagem.clienteId(), mensagem.nome());
            case "CLIENTE_SITUACAO_ALTERADA" -> alterarSituacaoDoTitular.executar(mensagem.clienteId(),
                    mensagem.bloqueado());
            case "CLIENTE_REMOVIDO" -> encerrarConta.executar(mensagem.clienteId());
            default -> LOG.warn("Evento de cliente ignorado: {}", mensagem.evento());
        }
    }
}
