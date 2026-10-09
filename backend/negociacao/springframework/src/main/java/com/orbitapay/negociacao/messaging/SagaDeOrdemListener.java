package com.orbitapay.negociacao.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.orbitapay.negociacao.application.usecase.CancelarCompra;
import com.orbitapay.negociacao.application.usecase.CancelarVenda;
import com.orbitapay.negociacao.application.usecase.ConfirmarCompra;
import com.orbitapay.negociacao.application.usecase.ConfirmarVenda;
import com.orbitapay.negociacao.messaging.mensagem.RespostaDaSagaMensagem;

@Component
public class SagaDeOrdemListener {

    private static final Logger LOG = LoggerFactory.getLogger(SagaDeOrdemListener.class);

    private final ConfirmarCompra confirmarCompra;
    private final CancelarCompra cancelarCompra;
    private final ConfirmarVenda confirmarVenda;
    private final CancelarVenda cancelarVenda;

    public SagaDeOrdemListener(ConfirmarCompra confirmarCompra, CancelarCompra cancelarCompra,
            ConfirmarVenda confirmarVenda, CancelarVenda cancelarVenda) {
        this.confirmarCompra = confirmarCompra;
        this.cancelarCompra = cancelarCompra;
        this.confirmarVenda = confirmarVenda;
        this.cancelarVenda = cancelarVenda;
    }

    @RabbitListener(queues = "${mensageria.assinaturas.debito-aprovado.fila}")
    public void debitoAprovado(@Payload RespostaDaSagaMensagem mensagem) {
        LOG.info("RECEBIDO conta.debito-aprovado {}", mensagem);
        confirmarCompra.executar(mensagem.ordemId());
    }

    @RabbitListener(queues = "${mensageria.assinaturas.debito-recusado.fila}")
    public void debitoRecusado(@Payload RespostaDaSagaMensagem mensagem) {
        LOG.info("RECEBIDO conta.debito-recusado {}", mensagem);
        cancelarCompra.executar(mensagem.ordemId(), mensagem.motivo());
    }

    @RabbitListener(queues = "${mensageria.assinaturas.acoes-reservadas.fila}")
    public void acoesReservadas(@Payload RespostaDaSagaMensagem mensagem) {
        LOG.info("RECEBIDO carteira.acoes-reservadas {}", mensagem);
        confirmarVenda.executar(mensagem.ordemId());
    }

    @RabbitListener(queues = "${mensageria.assinaturas.acoes-insuficientes.fila}")
    public void acoesInsuficientes(@Payload RespostaDaSagaMensagem mensagem) {
        LOG.info("RECEBIDO carteira.acoes-insuficientes {}", mensagem);
        cancelarVenda.executar(mensagem.ordemId(), mensagem.motivo());
    }
}
