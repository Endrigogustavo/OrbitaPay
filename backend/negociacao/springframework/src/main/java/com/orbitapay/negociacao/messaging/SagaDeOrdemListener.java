package com.orbitapay.negociacao.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.orbitapay.negociacao.application.usecase.ConcluirOrdem;
import com.orbitapay.negociacao.messaging.mensagem.RespostaDaSagaMensagem;

@Component
public class SagaDeOrdemListener {

    private static final Logger LOG = LoggerFactory.getLogger(SagaDeOrdemListener.class);

    private final ConcluirOrdem concluirOrdem;

    public SagaDeOrdemListener(ConcluirOrdem concluirOrdem) {
        this.concluirOrdem = concluirOrdem;
    }

    @RabbitListener(queues = "${mensageria.assinaturas.debito-aprovado.fila}")
    public void debitoAprovado(@Payload RespostaDaSagaMensagem mensagem) {
        LOG.info("RECEBIDO conta.debito-aprovado {}", mensagem);
        concluirOrdem.pagamentoAprovado(mensagem.ordemId());
    }

    @RabbitListener(queues = "${mensageria.assinaturas.debito-recusado.fila}")
    public void debitoRecusado(@Payload RespostaDaSagaMensagem mensagem) {
        LOG.info("RECEBIDO conta.debito-recusado {}", mensagem);
        concluirOrdem.pagamentoRecusado(mensagem.ordemId(), mensagem.motivo());
    }

    @RabbitListener(queues = "${mensageria.assinaturas.acoes-reservadas.fila}")
    public void acoesReservadas(@Payload RespostaDaSagaMensagem mensagem) {
        LOG.info("RECEBIDO carteira.acoes-reservadas {}", mensagem);
        concluirOrdem.acoesReservadas(mensagem.ordemId());
    }

    @RabbitListener(queues = "${mensageria.assinaturas.acoes-insuficientes.fila}")
    public void acoesInsuficientes(@Payload RespostaDaSagaMensagem mensagem) {
        LOG.info("RECEBIDO carteira.acoes-insuficientes {}", mensagem);
        concluirOrdem.acoesIndisponiveis(mensagem.ordemId(), mensagem.motivo());
    }
}
